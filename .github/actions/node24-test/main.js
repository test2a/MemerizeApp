console.log('Node version:', process.version);
console.log('Running Node24 test action main.js');
try {
  // Print environment to help debug
  console.log('GITHUB_ACTION:', process.env.GITHUB_ACTION || '');
} catch (e) {
  console.error(e);
}
