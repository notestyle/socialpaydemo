const sha256 = require("sha256");
const fs = require("fs");
const { X509Certificate, publicEncrypt, constants } = require("crypto");

const invoice = {
  amount: 1,
  remarks: "Test",
  accountName: "Нэст эдүкэйшн ХХК",
  accountNumber: 1505187177,
};

function toHexString(byteArray) {
  return Array.from(byteArray, function (byte) {
    return ("0" + (byte & 0xff).toString(16)).slice(-2);
  }).join("");
}

const getPublicKey = (async = (filename) => {
  const x509 = new X509Certificate(fs.readFileSync(filename));
  return x509.publicKey;
});

const encrypt = (data, publicKey) => {
  var encrypted = publicEncrypt(
    { key: publicKey, padding: constants.RSA_PKCS1_PADDING },
    data
  );
  return encrypted;
};

const sign = (text) => {
  const hash = sha256(text);
  console.log(hash);
  const hexHash = hash.toString(16).toLowerCase();
  const publicKey = getPublicKey("publicCertGLMT.crt");

  return encrypt(hexHash, publicKey);
};

const checksum = toHexString(sign(JSON.stringify(invoice)));

console.log(checksum);
