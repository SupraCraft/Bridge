#!/usr/bin/env bash
set -euo pipefail

MODE="${1:---dry-run}"
DEFAULT_BRANCH="${2:-master}"
EXCEPTIONS_FILE="maintenance/retired-branch-exceptions.tsv"

case "$MODE" in
  --dry-run|--apply) ;;
  *) echo "usage: $0 [--dry-run|--apply] [default-branch]" >&2; exit 2 ;;
esac

git fetch --prune origin "+refs/heads/*:refs/remotes/origin/*"
DEFAULT_REF="refs/remotes/origin/${DEFAULT_BRANCH}"
git rev-parse --verify "$DEFAULT_REF" >/dev/null

repo="${GITHUB_REPOSITORY:-}"
owner="${repo%%/*}"

is_explicitly_retired() {
  local branch="$1" sha="$2" recorded_branch recorded_sha evidence
  [[ -f "$EXCEPTIONS_FILE" ]] || return 1
  while IFS=$'\t' read -r recorded_branch recorded_sha evidence; do
    [[ -n "$recorded_branch" && "${recorded_branch:0:1}" != "#" ]] || continue
    if [[ "$recorded_branch" == "$branch" && "$recorded_sha" == "$sha" ]]; then
      return 0
    fi
  done < "$EXCEPTIONS_FILE"
  return 1
}

is_retired_branch() {
  local branch="$1"
  local ref="refs/remotes/origin/${branch}"
  local sha
  sha="$(git rev-parse "$ref")"

  if git merge-base --is-ancestor "$ref" "$DEFAULT_REF"; then
    return 0
  fi

  if [[ -n "$repo" && -n "${GH_TOKEN:-}" ]] && command -v gh >/dev/null 2>&1; then
    if [[ "$(gh api --method GET "repos/${repo}/pulls" \
      -f state=closed \
      -f "head=${owner}:${branch}" \
      --jq "map(select(.merged_at != null and .head.sha == \"${sha}\")) | length")" != "0" ]]; then
      return 0
    fi
  fi

  is_explicitly_retired "$branch" "$sha"
}

candidates=()
while IFS= read -r branch; do
  [[ -n "$branch" ]] || continue
  [[ "$branch" == "$DEFAULT_BRANCH" ]] && continue
  [[ "$branch" == "HEAD" ]] && continue
  if is_retired_branch "$branch"; then
    candidates+=("$branch")
  fi
done < <(git for-each-ref --format='%(refname:strip=3)' refs/remotes/origin | LC_ALL=C sort)

if ((${#candidates[@]} == 0)); then
  echo "No proven retired remote branches to prune."
  exit 0
fi

printf 'Proven retired branch candidates (%d):\n' "${#candidates[@]}"
printf '  %s\n' "${candidates[@]}"

if [[ "$MODE" == "--dry-run" ]]; then
  echo "Dry run only; no remote refs changed."
  exit 0
fi

for branch in "${candidates[@]}"; do
  git fetch --prune origin "+refs/heads/${DEFAULT_BRANCH}:refs/remotes/origin/${DEFAULT_BRANCH}" "+refs/heads/${branch}:refs/remotes/origin/${branch}"
  if ! is_retired_branch "$branch"; then
    echo "Refusing to delete ${branch}: retirement proof no longer matches live state." >&2
    exit 1
  fi
  echo "Deleting proven retired remote branch: ${branch}"
  git push origin --delete "$branch"
done
