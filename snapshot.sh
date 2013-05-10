PATH=/bin:/usr/bin:/usr/local/git/bin
mvn -DaltDeploymentRepository=snapshot-repo::default::file:../kfsAsnDecode-mvn/snapshots/ clean deploy
cd ../kfsAsnDecode-mvn
git add *
git commit -m 'snapshot'
git push origin master
