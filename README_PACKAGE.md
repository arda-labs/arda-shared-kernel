# 📦 Using arda-shared-kernel from GitHub Packages

## Automatic Updates

This package is automatically published to GitHub Packages when code is pushed to the `main` branch via GitHub Actions.

## Installation

### Step 1: Configure Maven Settings

Add GitHub Packages repository to your `~/.m2/settings.xml` or project's `pom.xml`:

**Option A: Global settings (`~/.m2/settings.xml`)**

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

**Option B: Project settings (add to `pom.xml`)**

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/arda-labs/arda-shared-kernel</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

### Step 2: Add Dependency

Add to your `pom.xml`:

```xml
<dependency>
  <groupId>vn.io.arda</groupId>
  <artifactId>arda-shared-kernel</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Step 3: Create GitHub Personal Access Token

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with these permissions:
   - `read:packages` (to download packages)
   - `write:packages` (if you need to publish packages)
3. Copy the token and use it as `YOUR_GITHUB_TOKEN` in settings.xml

## Environment Variables (CI/CD)

For GitHub Actions or other CI/CD:

```yaml
env:
  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

The `GITHUB_TOKEN` secret is automatically available in GitHub Actions.

## Update to Latest Version

Since this is a SNAPSHOT version, Maven will check for updates periodically. To force update:

```bash
mvn clean install -U
```

The `-U` flag forces Maven to update snapshots.

## Verify Installation

Check that the package is downloaded:

```bash
ls ~/.m2/repository/vn/io/arda/arda-shared-kernel/
```

## Troubleshooting

### Authentication Failed

- Verify your GitHub token has `read:packages` permission
- Check that username and token are correct in `~/.m2/settings.xml`
- Token must be a Personal Access Token (classic), not a password

### Package Not Found

- Ensure the repository URL is correct
- Verify you have access to the `arda-labs/arda-shared-kernel` repository
- Check if the package version exists in GitHub Packages

### Force Re-download

```bash
# Remove cached package
rm -rf ~/.m2/repository/vn/io/arda/arda-shared-kernel/

# Re-download
mvn clean install -U
```

## Latest Version

Check the latest version at:
- https://github.com/arda-labs/arda-shared-kernel/packages/
- https://github.com/arda-labs/arda-shared-kernel/releases/

---

**Note**: Every push to `main` branch automatically publishes a new SNAPSHOT version. Use `-U` flag or wait for Maven's update interval to get the latest changes.
