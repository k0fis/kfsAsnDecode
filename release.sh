PATH=/bin:/usr/bin:/usr/local/git/bin
mvn -DaltDeploymentRepository=repo::default::file:../kfsAsnDecode-mvn/release clean deploy 
cd ../kfsAsnDecode-mvn
git add *
git commit -m 'release'
git push origin master