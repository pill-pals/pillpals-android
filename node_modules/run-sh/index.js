const { exec } = require('child_process');

module.exports = function(cmd) {
  return new Promise(function (resolve, reject) {
    exec(cmd, function execCallback(err, stdout, stderr) {
      if (err) {
        console.log(stderr)
        reject(err);
      } else {
        // Strips newline at end
        stdout = stdout.replace(/\n$/, '');
        stderr = stderr.replace(/\n$/, '');
        resolve({ stdout, stderr });
      }
    });
  });
}
