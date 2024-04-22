# Auth2 client for Java

This repo contains a minimal client for the [KBase Auth2 server](https://github.com/kbase/auth2),
covering only the most common operations - e.g. validating tokens and user names. For those
functions, it is backwards compatible with the prior version of the client.

Most other uses are easily done with any http/REST client like the built in Java client
in 11+ or the Jersey client.

## Including the client in your build

See https://jitpack.io/#kbase/auth2_client_java for instructions on how to include JitPack
built dependencies in your build.

## JavaDoc

JavaDoc is available at
```
https://javadoc.jitpack.io/com/github/kbase/auth2_client_java/<version>/javadoc/
```

For example:

https://javadoc.jitpack.io/com/github/kbase/auth2_client_java/0.5.0/javadoc/

## Usage

If backwards compatibility with versions of the client prior to 0.5.0 is required, use the
`us.kbase.auth.ConfigurableAuthService` class. Otherwise use the
`us.kbase.auth.client.AuthClient` class.

Usage is fairly simple given a basic understanding of the auth2 server API - consult the
JavaDocs for details.

## Development

### Adding and releasing code

* Adding code
  * All code additions and updates must be made as pull requests directed at the develop branch.
    * All tests must pass and all new code must be covered by tests.
    * All new code must be documented appropriately
      * Javadoc
      * General documentation if appropriate
      * Release notes
* Releases
  * The main branch is the stable branch. Releases are made from the develop branch to the main
    branch.
  * Tag the version in git and github.
  * Create a github release.
  * Check that the javadoc is appropriately built on JitPack.

### Testing

Copy `test.cfg.example` to `test.cfg` and fill it in appropriately. Then:

```
./gradlew test
```

## Prior version

The prior version of the client is available at https://github.com/kbase/auth for source code
and in https://github.com/kbase/jars for built jars.
