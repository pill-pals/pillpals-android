# run-sh
https://www.npmjs.com/package/run-sh
## Install
`npm install run-sh`

## Use
```
const sh = require('run-sh');

sh("times").then(function(res) {
  console.log("Current server time: ", res.stdout);
  if (res.stderr) console.log("Times threw following err: ", res.stderr);
}, function(err) {
  console.log("Error running `times`: " err);
});
```
