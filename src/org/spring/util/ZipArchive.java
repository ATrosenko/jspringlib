package org.spring.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipArchive {

	public File ZipTarget;
	public File BasePath;
	public File Source;
	public FileFilter Filter;
	public boolean Checksum;
	public String Comment;
	public int BufferSize;

	protected class progress {
		public String CurrentFile;
		public long BytesCompressed;
	}

	public progress Progress;

	protected ZipOutputStream zip;

	public ZipArchive() throws IOException {
		this( new File( "" ) );
	}

	public ZipArchive( File f ) throws IOException {
		ZipTarget = f;
		BasePath = new File( "" );
		Source = null;
		Filter = null;
		Checksum = true;
		Comment = "";
		BufferSize = 16384;
		Progress = new progress();
		Progress.CurrentFile = "";
		Progress.BytesCompressed = 0;
		zip = new ZipOutputStream( new FileOutputStream( f ) );
		zip.setLevel( 9 );
	}

	public void Execute() throws IOException {
		this.Execute( Source );
	}

	public void Execute( File src ) throws IOException {
		zip.setComment( Comment );
		CompressFile( src );
		zip.close();
	}

	protected void CompressFile( File src ) throws IOException {
		byte[] buf = new byte[BufferSize];
		if( src.exists() ) {
			if( src.isDirectory() ) {
				File dir[] = src.listFiles( Filter );
				for( int i = 0; i < dir.length; i++ ) {
					CompressFile( dir[i] );
				}
			} else {
				try {
					String zename = src.getAbsolutePath().substring(
							BasePath.getAbsolutePath().length() + File.separator.length() );
					Progress.CurrentFile = zename;
					Progress.BytesCompressed = 0;
					ZipEntry ze = new ZipEntry( zename );
					ze.setTime( src.lastModified() );
					int len;
					if( Checksum ) {
						CheckedInputStream crcin =
								new CheckedInputStream( new FileInputStream( src ), new Adler32() );
						while( crcin.read( buf ) > 0 ) {
							Thread.currentThread().yield();
						}
						ze.setCrc( crcin.getChecksum().getValue() );
					}
					zip.putNextEntry( ze );
					FileInputStream in = new FileInputStream( src );
					while( ( len = in.read( buf ) ) > 0 ) {
						zip.write( buf, 0, len );
						Progress.BytesCompressed += len;
						Thread.currentThread().yield();
					}
					zip.closeEntry();
					in.close();
					Progress.CurrentFile = "";
					Progress.BytesCompressed = 0;
				} catch( IOException e ) {
					System.out.println( e.toString() );
					return;
				}
			}
		} else {
			throw( new IOException() );
		}
	}

}
