# Explanation of the Groovy script

# Explanation of the Groovy Init Script

This Groovy script decrypts a PGP-encrypted file, extracts a script from it, compiles the script dynamically, and saves
it for execution. Below is an explanation of its functionality:

---

## 1. **Importing Required Libraries**

The script imports:

- **BouncyCastle** classes for PGP encryption/decryption:
    - `ArmoredInputStream`, `PGPEncryptedDataList`, `PGPLiteralData`, etc.
- **Groovy** classes for dynamic script execution:
    - `CompilerConfiguration`, `GroovyShell`, etc.
- **Java Security**:
    - Adds the BouncyCastle security provider for cryptographic operations.

---

## 2. **Pipeline Parameters**

The script reads three parameters using `sdc.pipelineParameters()`:

- `pgpPassphrase`: Passphrase to decrypt the PGP private key.
- `pgpPrvKeyPath`: File path to the PGP private key.
- `pgpEncryptedCodePath`: Path to the encrypted file containing the Groovy script.

These parameters are used throughout the decryption process.

---

## 3. **Groovy Compiler Configuration**

The script sets up a custom Groovy compiler:

- **Source Encoding**: UTF-8.
- **Target Bytecode**: Java 17.
- **Custom Imports**: Adds utility imports (e.g., `java.util.*`) using `ImportCustomizer`.

This configuration ensures compatibility with modern Java versions and simplifies script processing.

---

## 4. **Reading the PGP Secret Key**

The script defines a static method `readSecretKeyRingCollection` to:

- Load a PGP private key from a file (supports `.asc` and `.gpg` formats).
- Use `ArmoredInputStream` or binary processing depending on the file type.
- Return a `PGPSecretKeyRingCollection` object for use in decryption.

---

## 5. **Processing the Encrypted File**

The encrypted file (`pgpEncryptedCodePath`) is:

- Opened as a stream and passed to a `JcaPGPObjectFactory` for PGP object parsing.
- Searched for a `PGPEncryptedDataList` object containing encrypted data.
- Extracted to retrieve the `PGPPublicKeyEncryptedData`.

If no `PGPEncryptedDataList` is found, the script throws an exception.

---

## 6. **Decrypting the Encrypted Data**

The decryption process includes:

1. **Finding the Matching Private Key**:
    - Uses the private key file (`pgpPrvKeyPath`) and passphrase to find the corresponding private key (
      `PGPPrivateKey`).
    - If no matching key is found, an exception is thrown.

2. **Decrypting the Data Stream**:
    - Decrypts the encrypted data using the extracted private key.
    - Processes the decrypted stream using a `JcaPGPObjectFactory`.

---

## 7. **Handling the Decrypted Message**

The decrypted message is processed as follows:

- **Compressed Data**:
    - If the decrypted data is compressed (`PGPCompressedData`), it is decompressed and further processed.
- **Literal Data**:
    - If the decrypted data contains literal text (`PGPLiteralData`), the script reads it into a `StringBuilder`.

If the message type is unexpected, the script throws an exception.

---

## 8. **Compiling the Decrypted Script**

- The decrypted script (stored in a `StringBuilder`) is dynamically compiled using `GroovyShell.parse()`.
- This results in a `Script` object that can be executed or saved for future use.

---

## 9. **Cleaning Up Resources**

The script ensures that all input streams (`fileInputStream`, `decoderStream`, etc.) are properly closed to release
system resources.

---

## Summary of Steps

1. **Read Pipeline Parameters**:
    - Obtain the passphrase, private key, and encrypted file path.
2. **Decrypt Encrypted Data**:
    - Use the BouncyCastle library to decrypt the PGP-encrypted file.
3. **Extract and Process Script**:
    - Decrypt, decompress (if necessary), and read the script.
4. **Compile the Script**:
    - Dynamically compile the extracted script for further execution.
5. **Resource Management**:
    - Close all streams to ensure proper cleanup.

---

## Key Error Handling

- If no `PGPEncryptedDataList` is found in the file, the script throws an exception.
- If no matching secret key is identified, it throws an exception.
- If the message type is unexpected, it raises an error indicating the unsupported type.

---

## Use Case

This script is ideal for workflows where:

- Scripts are securely encrypted with PGP.
- Encrypted scripts need to be dynamically decrypted, compiled, and executed.
- Integration with platforms like **StreamSets Data Collector (SDC)** is required.

# - [pgp_init_script.groovy](pgp_init_script.groovy)

# - [pgp_script.groovy](pgp_script.groovy)