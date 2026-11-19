#!/usr/bin/env bash
# Verify the app/dSYM UUID match and retain the exact symbol set from an Xcode archive.
set -euo pipefail
loom_archive_path=${1:?Pass the xcarchive path}
loom_symbols_output=${2:?Pass the output directory}
loom_app_binary="$loom_archive_path/Products/Applications/Loom.app/Loom"
mkdir -p "$loom_symbols_output"
loom_symbols_temporary=$(mktemp -d "$loom_symbols_output/.verify-dsym.XXXXXX")
trap 'rm -rf -- "$loom_symbols_temporary"' EXIT

dwarfdump --uuid "$loom_app_binary" | awk '{print $2, $3}' | sort -u > "$loom_symbols_temporary/binary.txt"
for loom_dsym_bundle in "$loom_archive_path"/dSYMs/*.dSYM; do
    if [ -d "$loom_dsym_bundle" ]; then
        dwarfdump --uuid "$loom_dsym_bundle" | awk '{print $2, $3}' >> "$loom_symbols_temporary/dsyms.txt"
    fi
done
if [ ! -s "$loom_symbols_temporary/binary.txt" ] || [ ! -s "$loom_symbols_temporary/dsyms.txt" ]; then
    printf '%s\n' 'The release archive must contain app UUIDs and generated dSYMs.' >&2
    exit 1
fi
sort -u "$loom_symbols_temporary/dsyms.txt" > "$loom_symbols_temporary/symbols.txt"
comm -23 "$loom_symbols_temporary/binary.txt" "$loom_symbols_temporary/symbols.txt" > "$loom_symbols_temporary/missing.txt"
if [ -s "$loom_symbols_temporary/missing.txt" ]; then
    printf '%s\n' 'The archived dSYMs do not match the app binary:' >&2
    cat "$loom_symbols_temporary/missing.txt" >&2
    exit 1
fi
cp "$loom_symbols_temporary/symbols.txt" "$loom_symbols_output/dsym-uuids.txt"
cp "$loom_archive_path/Info.plist" "$loom_symbols_output/archive-info.plist"
ditto -c -k --keepParent "$loom_archive_path/dSYMs" "$loom_symbols_output/Loom-dSYMs.zip"
printf '%s\n' 'Verified and archived the matching iOS release dSYMs.'
