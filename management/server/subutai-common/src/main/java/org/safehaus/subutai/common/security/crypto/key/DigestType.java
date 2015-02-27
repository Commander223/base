package org.safehaus.subutai.common.security.crypto.key;


/**
 * Enumeration of Digest Types supported by the DigestUtil class.
 * 
 */
public enum DigestType
{
	MD2( "MD2", "1.2.840.113549.2.2", "MD2" ), MD4( "MD4",
	        "1.2.840.113549.2.4", "MD4" ), MD5( "MD5", "1.2.840.113549.2.5",
	        "MD5" ), RIPEMD128( "RIPEMD128", "1.3.36.3.2.2", "RIPEMD-128" ), RIPEMD160(
	        "RIPEMD160", "1.3.36.3.2.1", "RIPEMD-160" ), RIPEMD256(
	        "RIPEMD256", "1.3.36.3.2.3", "RIPEMD-256" ), SHA1( "SHA1",
	        "1.3.14.3.2.26", "SHA-1" ), SHA224( "SHA-224",
	        "2.16.840.1.101.3.4.2.4", "SHA-224" ), SHA256( "SHA-256",
	        "2.16.840.1.101.3.4.2.1", "SHA-256" ), SHA384( "SHA-384",
	        "2.16.840.1.101.3.4.2.2", "SHA-384" ), SHA512( "SHA-512",
	        "2.16.840.1.101.3.4.2.3", "SHA-512" );

	private String jce;
	private String oid;
	private String friendly;


	private DigestType(String jce, String oid, String friendly)
	{
		this.jce = jce;
		this.oid = oid;
		this.friendly = friendly;
	}


	/**
	 * Get digest type JCE name.
	 * 
	 * @return JCE name
	 */
	public String jce()
	{
		return jce;
	}


	/**
	 * Get digest type Object Identifier.
	 * 
	 * @return Object Identifier
	 */
	public String oid()
	{
		return oid;
	}


	/**
	 * Get signature type friendly name.
	 * 
	 * @return Friendly name
	 */
	public String friendly()
	{
		return friendly;
	}


	/**
	 * Resolve the supplied JCE name to a matching Digest type.
	 * 
	 * @param jce
	 *            JCE name
	 * @return Digest type or null if none
	 */
	public static DigestType resolveJce( String jce )
	{
		for ( DigestType digestType : values() )
		{
			if ( jce.equals( digestType.jce() ) )
			{
				return digestType;
			}
		}

		return null;
	}


	/**
	 * Returns friendly name.
	 * 
	 * @return Friendly name
	 */
	public String toString()
	{
		return friendly();
	}
}
