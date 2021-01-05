/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.encryptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import controller.License;

/**
 *
 * @author josea
 */
public class CifraHibrida {

    Simetrica sim = new Simetrica();
    Assimetrica assim = new Assimetrica();
    License l = new License();
    
    public void encriptar(String texto, String publicKey) throws FileNotFoundException, IOException, Exception {

        // Secret Key Generation
        KeyGenerator keyGenerator;

        File encryptedFile = null;

        try {
            SecretKey secretKey = sim.generateKey();
            byte[] secretkeyByte = secretKey.getEncoded();
            /*System.out.println("Secret Key:");
            System.out.println(Arrays.toString(secretkeyByte));*/

            // Ler conteúdo
            byte[] contentBytes = texto.getBytes();

            //encripttar texto em claro
            byte[] encDataBytes = Simetrica.encriptar(secretKey, contentBytes);
            /*System.out.println("\n@Encrypted Content Bytes");
            System.out.println(Arrays.toString(encDataBytes));*/

            //Encriptar chave
            byte[] encSecretkey = Assimetrica.encriptar(secretkeyByte, publicKey);
            /*System.out.println("\n@Encrypted Secret Key : ");
            System.out.println(Arrays.toString(encSecretkey));*/

            //Para bytes
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] base64EncSecKey = encoder.encode(encSecretkey);
            byte[] base64EncData = encoder.encode(encDataBytes);

            //Para String
            String encSecretkeyString = new String(base64EncSecKey, StandardCharsets.UTF_8);
            String encDataString = new String(base64EncData, StandardCharsets.UTF_8);
            /*System.out.println("\n@Encrypted Secret Key String: ");
            System.out.println(encSecretkeyString);
            System.out.println("\n@Encrypted Data String: ");
            System.out.println(encDataString);*/

            //Conteudo do ficheiro
            StringBuilder sb = new StringBuilder();
            sb.append(encSecretkeyString).append(System.lineSeparator()).append(encDataString);

            // Criação do ficheiro
            String encFilePath = "license.txt";
            encryptedFile = new File(encFilePath);
            FileWriter writer;
            writer = new FileWriter(encryptedFile);
            BufferedWriter bufWriter = new BufferedWriter(writer);
            bufWriter.write(sb.toString());
            bufWriter.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public String decriptar(String path, String pvtKey) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, Exception {
        File inFile = new File(path);
        String[] contents = extractDataElements(inFile);

        //System.out.println("Linhas: " + contents.length);
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encSecretKeyByte = decoder.decode(contents[0]);
        //desencriptar chave simetrica
        byte[] secretKeyBytes = Assimetrica.decriptar(encSecretKeyByte, pvtKey);
        /*System.out.println("Secret Key Bytes: ");
        System.out.println(Arrays.toString(secretKeyBytes));*/
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length, "AES");

        //desencriptar o texto em claro
        byte[] encStrByte = decoder.decode(contents[1]);
        byte[] messageByte = Simetrica.decriptar(secretKey, encStrByte);
        /*System.out.println("Texto em bytes: ");
        System.out.println(Arrays.toString(messageByte));*/
        contents[1] = new String(messageByte, StandardCharsets.UTF_8);

        return messageByte.toString();
    }

    private static String[] extractDataElements(File file) {
        String[] dataArr = null;
        FileReader reader;
        BufferedReader bufReader;
        try {
            char nwLine = '\n';
            char cr = '\r';
            reader = new FileReader(file);
            bufReader = new BufferedReader(reader);
            //Extrair chave
            String encSecretKey = bufReader.readLine();
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[10];
            int nChars = bufReader.read(buffer);
            while (nChars > 0) {
                for (int i = 0; i < nChars; i++) {
                    sb.append(buffer[i]);
                }
                nChars = bufReader.read(buffer);
            }
            dataArr = new String[2];
            dataArr[0] = encSecretKey;
            dataArr[1] = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataArr;
    }
}