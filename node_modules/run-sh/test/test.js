const sh = require('../');

it('Echos single line', async () => {
  expect.assertions(1);
  const res = await sh("echo test");
  expect(res.stdout).toEqual('test');
});

it('Echos double line', async () => {
  expect.assertions(1);
  const res = await sh("echo $'test\nmore'");
  expect(res.stdout).toEqual("test\nmore");
});
