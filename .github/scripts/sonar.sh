echo "coverage_percentage=not_available" >> "$GITHUB_OUTPUT"

SONAR_BASE_URL="${SONAR_HOST_URL%/}"

if ! RESPONSE="$(
  curl --fail --show-error --silent \
    --get "$SONAR_BASE_URL/api/measures/component" \
    --header "Authorization: Bearer $SONAR_API_TOKEN" \
    --data-urlencode "component=$SONAR_PROJECT_KEY" \
    --data-urlencode "metricKeys=coverage"
)"; then
  echo "::warning::SonarQube coverage 조회에 실패했습니다."
  exit 0
fi

COVERAGE="$(
  echo "$RESPONSE" |
    jq -r '.component.measures[]
      | select(.metric == "coverage")
      | .value'
)"

if [ -z "$COVERAGE" ] || [ "$COVERAGE" = "null" ]; then
  echo "::warning::SonarQube 응답에 coverage 값이 없습니다."
  exit 0
fi

echo "coverage_percentage=$COVERAGE" >> "$GITHUB_OUTPUT"