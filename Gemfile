source "https://rubygems.org"

gem "fastlane"

# Security fixes for CVEs in fastlane transitive dependencies
gem "addressable", ">= 2.9.0"   # CVE-2026-35611 ReDoS
gem "json", ">= 2.19.2"         # CVE-2026-33210 Format String
gem "jwt", ">= 3.2.0"           # CVE-2026-45363 Improper Authentication
gem "faraday", ">= 1.10.6"      # CVE-2026-25765 SSRF, SNYK-RUBY-FARADAY-17400242

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
