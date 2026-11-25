# VyuhStack example: Spring Boot

A Spring Boot web app built with Maven and run by systemd. The page and
`/health` show the commit the process was started from, so you can see each
push go live.

## One-time VM setup

Run on the VM as a user with sudo (for example `vyuh-user`):

```bash
sudo apt update && sudo apt install -y git openjdk-21-jdk-headless maven

# deploys run as vyuh-user in a non-interactive shell: this must print paths
sudo runuser -l vyuh-user -c 'command -v java mvn && java -version'
```

A Maven build plus the running app need more than 1 GB of memory. Use a Small
flavor or bigger, or add swap on a Nano VM:

```bash
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

Then connect the repo in VyuhStack (settings below) and click **Verify**, which
clones it into `/opt/app`. After that, install the service:

```bash
sudo cp /opt/app/deploy/app.service /etc/systemd/system/app.service
sudo systemctl daemon-reload
sudo systemctl enable app
```

The first deploy builds the jar and starts it.

## Deploy from Git settings

| Field | Value |
|---|---|
| Repository URL | `https://github.com/Vyuhstack/vyuhstack-example-spring.git` |
| Branch | `main` |
| Folder on VM | `/opt/app` |
| Run as user | `vyuh-user` |
| Build | `export MAVEN_OPTS="-Xmx512m"`<br>`mvn -q -B clean package -DskipTests` |
| Release | `install -m 644 target/app.jar app.jar.new`<br>`mv -f app.jar.new app.jar`<br>`sudo -n systemctl restart app` |

Application port in VyuhStack: `8080`.

The release step copies the jar and renames it into place. The rename is atomic,
so the running app keeps its old jar until the restart, and a failed build never
touches it. The first build downloads the Maven dependencies and is slow; later
builds reuse `~/.m2`.

`sudo -n` needs passwordless sudo for `vyuh-user`. VyuhStack VMs have it by
default; check with `sudo runuser -l vyuh-user -c 'sudo -n true && echo ok'`.
Otherwise add a rule with `sudo visudo -f /etc/sudoers.d/vyuhstack-deploy`:

```
vyuh-user ALL=(root) NOPASSWD: /usr/bin/systemctl restart app
```

## Check it

```bash
curl -s http://<vm-hostname>/health
```

Push a change and the `commit` value updates after the deploy finishes.

## Try a failed build

Break the Java code (for example remove a semicolon in `HelloController.java`)
and push. The deploy fails at `mvn package`, the release step doesn't run, and
the running app keeps serving the old commit.
