<!-- TOC -->

* [Feature 3 logic](#feature-3-logic)
    * [Init Script](#init-script)
    * [Script](#script)
    * [Destroy Script](#destroy-script)

<!-- TOC -->

# Feature 3 logic

This Script is using the technic that has been seen in the Feature 1. The only
difference is that the Groovy code will not be generated at run-time but loaded
from a PGP encrypted file.
PGP encryption is used but any type of encryption could be used. The demo is
using an RSA key of 4096 bits long.

## Init Script

- The logic is having 2 sides.
    - Logic to read and decrypt the Groovy / Java code in memory
    - Compile the code and make the code available to the `script` for execution

```
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

String pgpPassphrase = sdc.pipelineParameters()['pgpPassphrase']
String pgpKeyPath = sdc.pipelineParameters()['pgpPrvKeyPath']
def inputFilePath = sdc.pipelineParameters()['pgpEnryptedCodePath']

ImportCustomizer importCustomizer = new ImportCustomizer()
importCustomizer.addStarImports("java.util")
importCustomizer.addStarImports("com.streamsets")
importCustomizer.addStarImports("com.streamsets.pipeline")
importCustomizer.addStarImports("com.streamsets.pipeline.api")

def script = new StringBuilder()
def config = new CompilerConfiguration()
config.setSourceEncoding("UTF-8")
config.targetBytecode = "17.0"
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

// compile and save to the state store (sharing object with main & destroy code )
Script object = shell.parse(script.toString())

sdc.state['binding'] = binding
sdc.state["script"] = object
```

## Script

- As soon as the `Init Script` is executed then the `Script` will loop to
  consume and produce output records
- The `sdc.state['binding'].setVariable("record", record)` is used to expose the
  variable named record to the dynamic code
- The `sdc.state['run'].run()` method is activating the dynamic code logic
- Then `sdc.output.write(record)` will write the record for pipeline downstream
  usage

```
sdc.records.each { record ->
    try {
      sdc.state['binding'].setVariable("record", record)
      sdc.state['script'].run()
      sdc.output.write(record)
    } catch (e) {
        sdc.log.error(e.toString(), e)
        sdc.error.write(record, e.toString())
    }
}
```

## Destroy Script

- Cleanup memory resources

```
sdc.state['binding'] = null
sdc.state["script"] = null
```