#!/usr/bin/env python3
"""Reject development-version-specific artifact filenames in reusable workflows."""

from __future__ import annotations

import re
from pathlib import Path


def main() -> int:
    workflow = Path('.github/workflows/compatibility.yml').read_text(encoding='utf-8')
    hardcoded = re.findall(r'bridge(?:-test)?-\d+\.\d+\.\d+-dev\.jar', workflow)
    if hardcoded:
        unique = ', '.join(sorted(set(hardcoded)))
        raise SystemExit(f'Compatibility workflow hardcodes development artifact filenames: {unique}')
    required = (
        'help:evaluate -Dexpression=project.version',
        'BRIDGE_BUILD_VERSION',
        'build/bridge/bridge-${BRIDGE_BUILD_VERSION}.jar',
        'build/bridge-test/bridge-test-${BRIDGE_BUILD_VERSION}.jar',
    )
    for fragment in required:
        if fragment not in workflow:
            raise SystemExit(f'Compatibility workflow missing version-neutral fragment: {fragment}')
    print('Version-neutral compatibility workflow contract OK')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
