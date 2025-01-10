import org.bouncycastle.bcpg.ArmoredInputStream
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.*
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.security.Security

String pgpPassphrase = "groovy"
String pgpKeyPath = "/home/jmb/IdeaProjects/Groovy-Technics/keys/prvkey.asc"
def inputFilePath = "/home/jmb/IdeaProjects/Groovy-Technics/pgp/secretGroovyCode.pgp"

def script = new StringBuilder()
def config = new CompilerConfiguration()
config.recompileGroovySource = false
config.debug = false
config.setSourceEncoding("UTF-8")
config.targetBytecode = "17.0"
config.optimizationOptions
config.verbose = false
config.indyEnabled

ImportCustomizer importCustomizer = new ImportCustomizer()
importCustomizer.addStarImports("java.util")
config.addCompilationCustomizers(importCustomizer)
def binding = new Binding()
def shell = new GroovyShell(binding, config)


static PGPSecretKeyRingCollection readSecretKeyRingCollection(String privateKeyFilePath) {
    InputStream keyIn = new FileInputStream(new File(privateKeyFilePath))
    InputStream decodedInput = (privateKeyFilePath.endsWith(".asc") || privateKeyFilePath.endsWith(".gpg")) ? new ArmoredInputStream(keyIn) : keyIn
    PGPSecretKeyRingCollection secretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(decodedInput), new JcaKeyFingerprintCalculator())
    keyIn.close()
    return secretKeyRingCollection
}

Security.addProvider(new BouncyCastleProvider())
InputStream fileInputStream = new BufferedInputStream(new FileInputStream(inputFilePath))
InputStream decoderStream = PGPUtil.getDecoderStream(fileInputStream)
JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(decoderStream)
// Find the PGPEncryptedDataList object
PGPEncryptedDataList enc = null
Object pgpObj
while ((pgpObj = pgpF.nextObject()) != null) {
    if (pgpObj instanceof PGPEncryptedDataList) {
        enc = (PGPEncryptedDataList) pgpObj
        break
    }
}
if (enc == null) {
    throw new IllegalArgumentException("No PGPEncryptedDataList found in the input file")
}
// Retrieve the encrypted data object
PGPPublicKeyEncryptedData pbe = (PGPPublicKeyEncryptedData) enc.getEncryptedDataObjects().next()
// Retrieve private key
PGPSecretKeyRingCollection pgpSec = readSecretKeyRingCollection(pgpKeyPath)
PGPSecretKey secKey = pgpSec.getSecretKey(pbe.getKeyID())
if (secKey == null) {
    throw new IllegalArgumentException("No secret key found for the given key ID")
}
PGPPrivateKey sKey = secKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pgpPassphrase.toCharArray()))
// Decrypt the data
InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey))
JcaPGPObjectFactory plainFact = new JcaPGPObjectFactory(clear)
Object message = plainFact.nextObject()
// Handle compressed data if present
if (message instanceof PGPCompressedData) {
    PGPCompressedData compressedData = (PGPCompressedData) message
    JcaPGPObjectFactory compressedFact = new JcaPGPObjectFactory(compressedData.getDataStream())
    message = compressedFact.nextObject()
}
// Handle literal data
if (message instanceof PGPLiteralData) {
    PGPLiteralData ld = (PGPLiteralData) message
    InputStream unc = ld.getInputStream()
    BufferedReader reader = new BufferedReader(new InputStreamReader(unc))
    script.append(reader.lines().collect().join())
} else {
    throw new IllegalArgumentException("Unexpected PGP message type: " + message.getClass().getName())
}
// Clean up resources
decoderStream.close()
fileInputStream.close()

//println script.toString()
Script object = shell.parse(script.toString())
object.run()


System.exit(0)
