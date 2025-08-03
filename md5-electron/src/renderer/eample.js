const path = require('path')
const fs = require('fs')
const crypto = require('crypto')

//const buf = fs.readFileSync(path.join(__dirname, "centos.qcow2"));
//hash.update(buf, 'utf8')

const hash = crypto.createHash('md5')
const stream = fs.createReadStream("/home/cnki/README.md")
//const stream = fs.createReadStream(path.join(__dirname, "centos.qcow2"))
stream.on('data', chunk => {
    hash.update(chunk, 'utf8')
})

stream.on('end', () => {
    const md5sum = hash.digest('hex')
    console.log(md5sum)
})

const stats = fs.statSync('/home/cnki/README.md')
console.log(stats)
