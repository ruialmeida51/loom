const environments = { firebase: 'firebase-distribution', play: 'play-internal' };

/** Durable version history without creating Git tags or GitHub releases for Firebase builds. */
export class ReleaseHistory {
  constructor(repository, token, request = fetch) {
    if (!/^[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+$/.test(repository)) throw new Error('Invalid GitHub repository.');
    this.api = `https://api.github.com/repos/${repository}`;
    this.token = token;
    this.request = request;
  }

  async call(path, options = {}) {
    const response = await this.request(`${this.api}/${path}`, {
      ...options,
      headers: { authorization: `Bearer ${this.token}`, accept: 'application/vnd.github+json', 'X-GitHub-Api-Version': '2022-11-28', 'content-type': 'application/json' },
      signal: AbortSignal.timeout(30_000),
    });
    if (!response.ok) throw new Error(`GitHub release-history access failed (HTTP ${response.status}). The workflow needs deployments: write and contents: write.`);
    return response.json();
  }

  async latest(destination) {
    for (let page = 1; ; page++) {
      const deployments = await this.call(`deployments?environment=${environments[destination]}&per_page=100&page=${page}`);
      for (const deployment of deployments) {
        const metadata = deployment.payload?.loomRelease;
        if (!metadata || metadata.destination !== destination) continue;
        const statuses = await this.call(`deployments/${deployment.id}/statuses?per_page=100`);
        if (statuses.some(status => status.state === 'success')) return metadata;
      }
      if (deployments.length < 100) return null;
    }
  }

  async versions() {
    return (await Promise.all(Object.keys(environments).map(destination => this.latest(destination)))).filter(Boolean);
  }

  async record({ destination, versionName, versionCode, sha, url }) {
    const environment = environments[destination];
    if (!environment || !/^[a-f0-9]{40}$/.test(sha)) throw new Error('Invalid successful-release metadata.');
    const deployment = await this.call('deployments', {
      method: 'POST', body: JSON.stringify({
        ref: sha, auto_merge: false, required_contexts: [], environment,
        production_environment: false, transient_environment: false,
        description: `Loom ${versionName} (${versionCode})`,
        payload: { loomRelease: { destination, versionName, versionCode } },
      }),
    });
    await this.call(`deployments/${deployment.id}/statuses`, {
      method: 'POST', body: JSON.stringify({ state: 'success', environment_url: url, description: `Loom ${versionName} (${versionCode})`, auto_inactive: false }),
    });
  }
}
