This document describes the steps to release a new version.

1. Update the version in the following files:

   * core/src/org/alexvsbus/Defs.java
   * build.gradle
   * android/build.gradle
   * README.md

2. Add a description about the new release to News.md.

3. Create a Git tag:

   ``git tag -a <version> -m <short description>``

4. Push to GitHub:

   ``git push --tags origin main``

5. Finally, create the release on GitHub with the same description added to
News.md.

