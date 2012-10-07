package com.modcrafting.ultrabans.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

public class RSAServerCrypto {
	private PublicKey publicKey;
	private PrivateKey privateKey;
	public RSAServerCrypto(File dataFolder){
		try {
			File pub = new File(dataFolder,"public.key");
			File priv = new File(dataFolder,"private.key");
			if(!pub.exists()||!priv.exists()){
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
				RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
				kpg.initialize(spec);
				KeyPair kp = kpg.genKeyPair();
				publicKey = kp.getPublic();
				privateKey = kp.getPrivate();
				try {
					savePublic(pub);
					savePrivate(priv);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			if(publicKey==null||privateKey==null){
				try {
					loadPublic(pub);
					loadPrivate(priv);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e1) {
			e1.printStackTrace();
		}
	}
	private void savePublic(File file) throws IOException{
		X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKey.getEncoded());
		FileOutputStream out = new FileOutputStream(file);
		out.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded()).getBytes());
		out.close();
	}
	private void savePrivate(File file) throws IOException{
		PKCS8EncodedKeySpec publicSpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
		FileOutputStream out = new FileOutputStream(file);
		out.write(DatatypeConverter.printBase64Binary(publicSpec.getEncoded()).getBytes());
		out.close();
	}
	private void loadPublic(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		byte[] encodedPublicKey = new byte[(int) file.length()];
		in.read(encodedPublicKey);
		encodedPublicKey = DatatypeConverter.parseBase64Binary(new String(encodedPublicKey));
		in.close();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
		publicKey = keyFactory.generatePublic(publicKeySpec);
	}
	private void loadPrivate(File file) throws Exception {
		FileInputStream in = new FileInputStream(file);
		byte[] encodedPrivateKey = new byte[(int) file.length()];
		in.read(encodedPrivateKey);
		encodedPrivateKey = DatatypeConverter.parseBase64Binary(new String(encodedPrivateKey));
		in.close();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
		privateKey = keyFactory.generatePrivate(privateKeySpec);
	}

	public byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	public byte[] decrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
}
