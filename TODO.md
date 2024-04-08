* Use a standard library for the string and token caches. The were copied from the original
  auth repo since they're known to work. Caffeine is nice
* Once updated to Java 11+, use the built in http client vs. urlconnection
* Start up a local auth server in test mode for tests, stop using GHA tokens
  * Means fork PRs can pass tests
  * Wait until the shadow jar is published on maven somewhere so we don't have to pollute the 
    build with the jars repo