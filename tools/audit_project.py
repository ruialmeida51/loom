#!/usr/bin/env python3
"""Check Loom's feature ownership, demo isolation, localization and Gradle boundaries."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
issues = []
edges = {}
source_count = 0
catalogs = 0
resource_count = 0
transport_count = 0

for build in (ROOT / 'shared/features').glob('*/build.gradle.kts'):
    owner = build.parent.name
    edges[owner] = set(re.findall(r'projects\.shared\.features\.([A-Za-z]+)', build.read_text()))
    assert build.parent.parent.name == 'features'
    if 'projects.shared.di' in build.read_text():
        issues.append(f'Feature depends on the composition module: {build.relative_to(ROOT)}')
    for nested in build.parent.rglob('build.gradle.kts'):
        if nested != build and 'build' not in nested.relative_to(build.parent).parts:
            issues.append(f'Layer Gradle module: {nested.relative_to(ROOT)}')

for build in ROOT.rglob('*.gradle.kts'):
    if any(p in ('build', '.gradle') for p in build.relative_to(ROOT).parts):
        continue
    if re.search(r'project\(\s*[\"\']:', build.read_text()):
        issues.append(f'String-based project dependency: {build.relative_to(ROOT)}')

for source in (ROOT / 'shared').rglob('*.kt'):
    path = source.relative_to(ROOT)
    if 'build' in path.parts:
        continue
    source_count += 1
    text = source.read_text()
    actual = re.search(r'^package\s+([^\s;]+)', text, re.M)
    expected = str(source.parent).split('/kotlin/', 1)[-1].replace('/', '.')
    if not actual or actual.group(1) != expected:
        issues.append(f'Package/path mismatch: {path}')
    common = '/commonMain/' in str(source)
    imports = re.findall(r'^import\s+([^\s;]+)', text, re.M)
    if common and path.parts[1] != 'di':
        if any(imp in (
            'org.koin.core.annotation.Module', 'org.koin.core.annotation.Single',
            'org.koin.core.annotation.KoinViewModel', 'org.koin.android.annotation.KoinViewModel',
        ) for imp in imports):
            issues.append(f'DI provider outside shared/di: {path}')
        if 'org.koin.dsl.module' in imports:
            issues.append(f'Manual DI registration outside shared/di: {path}')
    if any(imp.endswith('.*') for imp in imports):
        issues.append(f'Wildcard import: {path}')
    if common:
        if re.search(r'\b(?:class\s+Observe\w+UseCase|fun\s+observe(?:Expenses|Settlements)?\s*\()', text):
            issues.append(f'Obsolete observation API; use stream/Get*StreamUseCase: {path}')
        if '/data/' not in str(source):
            if re.search(r'"[a-z]+\.message_[a-z0-9_]+"', text):
                issues.append(f'Raw error protocol outside data: {path}')
            if re.search(r'\blocalizedMessage\s*\(', text):
                issues.append(f'String-based error localization: {path}')
            if re.search(r'\b(?:error|failure|exception|cause)\s*\?*\.\s*message\b', text):
                issues.append(f'Raw exception text outside data: {path}')
        if '/domain/errors/' in str(source):
            if not re.search(r'\b(?:abstract|sealed) class \w*Error', text) and source.name != 'RequireDomain.kt':
                issues.append(f'Error family must be a domain type: {path}')
        if '/presentation/viewmodel/' in str(source):
            if re.search(r'val\s+(?:error|message|undoMessage):\s*String\??', text):
                issues.append(f'UI messages must use TextResource: {path}')
            if re.search(r'(?:mutableState|inputState)\.update\s*\{\s*(?!oldState\s*->)\w+\s*->|(?:mutableState|inputState)\.update\s*\{\s*it\.', text):
                issues.append(f'Name the previous ViewModel state oldState: {path}')
            if re.search(r'\.observeWith\([^)]*\)\s*\{[^}]*mutableState\.update', text, re.S):
                issues.append(f'Use updateState inside observeWith: {path}')
        if '@Serializable' in text and not any(imp.endswith('.NavDestination') for imp in imports):
            transport_count += 1
            if not re.search(r'/data/network/model/(?:request|response)/', str(source)):
                issues.append(f'Transport model outside request/response package: {path}')
            declarations = re.findall(r'^\s*(?:internal )?data class (\w+)', text, re.M)
            if declarations != [source.stem]:
                issues.append(f'Transport model must have its own matching file: {path}')
            if len(re.findall(r'\bval\s+\w+\s*:', text)) != len(re.findall(r'@SerialName\(', text)):
                issues.append(f'Every transport field needs SerialName: {path}')
        if '/data/network/datasource/' in str(source) and re.search(r'@Serializable|\bdata class\b', text):
            issues.append(f'DataSource co-declares a transport model: {path}')
    if path.parts[1] == 'core':
        if any(re.search(r'com\.eyther\.lumbridge\.(features|app)\.', imp) for imp in imports):
            issues.append(f'Core depends on a feature/app: {path}')
    if '/domain/' in str(source) and common:
        for imp in imports:
            if re.search(r'\.data\.|\.presentation\.|generated\.resources|androidx\.|io\.ktor|jan\.supabase|compose\.resources', imp):
                issues.append(f'Domain imports infrastructure/UI: {path}: {imp}')
    if path.parts[1] == 'features':
        owner = path.parts[2]
        for dependency in set(re.findall(r'import com\.eyther\.lumbridge\.features\.([a-z]+)\.', text)) - {owner}:
            if dependency not in edges[owner]:
                issues.append(f'Undeclared direct dependency: {owner} -> {dependency}: {path}')
        if common and re.search(r'\bDemo[A-Z]\w*|context\.demo|\bif\s*\(\s*demo', text):
            issues.append(f'Demo behavior in production feature: {path}')
    if common and '/demo/' not in str(source) and 'com.eyther.lumbridge.app.demo.' in text:
        issues.append(f'Production source imports demo graph: {path}')
    if common and ('/presentation/screen/' in str(source) or '/presentation/components/' in str(source) or source.name in ('HouseholdHost.kt', 'LoomDemoApp.kt')):
        # Data identifiers, symbols and dynamic user content are not UI copy.
        if re.search(r'\b(?:Text|PageHeading|LoomButton|SectionTitle|EmptyNotice)\s*\(\s*"[^"\n]*[A-Za-z]', text):
            issues.append(f'Hardcoded UI copy: {path}')

visiting = set()
visited = set()
def visit(node):
    if node in visiting:
        issues.append(f'Feature dependency cycle at {node}')
        return
    if node in visited:
        return
    visiting.add(node)
    for child in edges.get(node, ()):
        visit(child)
    visiting.remove(node)
    visited.add(node)
for node in edges:
    visit(node)

result_source = ROOT / 'shared/core/src/commonMain/kotlin/com/eyther/lumbridge/core/domain/wrapper/AppResult.kt'
if not re.search(r'data class Failure\(val error: AppError\)', result_source.read_text()):
    issues.append('AppResult.Failure must carry AppError')

for xml in (ROOT / 'shared').glob('**/src/commonMain/composeResources/values*/strings.xml'):
    tree = ET.parse(xml).getroot()
    keys = [entry.get('name') for entry in tree]
    if len(keys) != len(set(keys)):
        issues.append(f'Duplicate resource keys: {xml.relative_to(ROOT)}')
    catalogs += 1
    resource_count += len(keys)

if issues:
    raise SystemExit('\n'.join(issues))
print(f'PASS: {source_count} Kotlin files; feature ownership and package paths match.')
print('PASS: domain imports, core boundaries, direct dependencies and acyclic feature graph.')
print('PASS: production sources contain no demo graph imports or demo feature branches.')
print('PASS: Gradle uses named project references; features contain no layer modules.')
print('PASS: DI providers stay in shared/di; feature modules do not depend on composition.')
print('PASS: typed domain failures, resource-backed UI messages, stream naming and explicit imports.')
print(f'PASS: {transport_count} transport models have separate files/packages and explicit wire field names.')
print(f'PASS: UI copy resource checks; {catalogs} catalogs with {resource_count} string/plural resources.')
