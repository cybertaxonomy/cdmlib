## Updating springfox-swagger-ui

springfox-swagger-ui needs to be downloaded from a maven repositoty. 
Make sure the version matches the version of the springfox-swagger dependency int the `cdmlib/pom.xml:`

~~~
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.6.1</version>
</dependency>
~~~

e.g.: https://search.maven.org/artifact/io.springfox/springfox-swagger-ui/2.6.1/jar

The files `./index.html` and `./webjars/springfox-swagger-ui/springfox.js` file have modifications which need to re-applied to the stock version of these file.


Extract the dowloaded jar it contains a `META-INF` folder. Now copy the update in place:

~~~
cp META-INF/swagger-ui.html index.html
find -not -name "*.diff" -prune -exec rm {} \; // remove all but the diffs which are needed later on
cp -r META-INF/webjars/springfox-swagger-ui/* cdmlib-remote-webapp/src/main/webapp/doc/webjars/springfox-swagger-ui/
~~~

Fist of all create a copies of `index.html` and `springfox.js` before overwriting them, these are needed later on for creating diffs of the applied modifications.

~~~
cp index.html index-orig.html
cp ./webjars/springfox-swagger-ui/springfox-orig.js
~~~

Now **MANUALLY** apply the chnages recorded in the diffs `index.html.diff` and  `springfox.js.diff`. Patching the files with the diff might not work since the diffs have been made from the last version.

Once you are ready and the swagger UI is is working properly with the updated version you need to create the diffs for the next update to come:


~~~
diff index-orig.html index.html > index.html.diff
cd ./webjars/springfox-swagger-ui/
diff springfox-last.js springfox.js > springfox.js.diff
~~~

**Congratulations, your are done and you can commit your work.**

content of the `cdmlib-remote-webapp/src/main/webapp/doc/webjars/springfox-swagger-ui/`
The `springfox.js` file has modifications which need to re-applied to the stock version of this file.


