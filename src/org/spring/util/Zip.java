package org.spring.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

	public File ZipFile; // Target ZIP file, REQUIRED
	public File Source; // Source dir/file, REQUIRED
	public File BasePath; // Source base path

	// (used to store relative path in ZIP)
	// Default: path to source
	public FileFilter Filter; // Source pathnames filter, Default: all files
	public boolean Checksum; // Store checksums, Default: true
	public String Comment; // ZIP comment, Default: empty
	public int Level; // ZIP compression level, Default: 9(MAX)
	public int RecursionDepth; // Directories recursion depth, Default: MAX

	// 0 = do not recurse, 1 = one level below, etc
	protected int BufferSize; // Read buffer size, Default: 16K
	protected boolean Priority; // Do not yield ZIP thread, Default: false

	protected class progress { // Used to watch for processing
		public String CurrentFile; // Pathname being ZIP-ed
		public long CurrentCompressed; // Bytes allready compressed in current file
		public long TotalCompressed; // Bytes allready compressed in all ZIP
	};
	public progress Progress; // Used to watch for processing

	protected ZipOutputStream zip;
	protected int CurrentRecursion;

	public Zip() throws IOException {
		ZipFile = null;
		BasePath = null;
		Source = null;
		Filter = null;
		Checksum = true;
		Comment = "";
		Level = 9;
		BufferSize = 16384;
		Priority = false;
		Progress = new progress();
		Progress.CurrentFile = "";
		Progress.CurrentCompressed = 0;
		Progress.TotalCompressed = 0;
	};

	public void Execute() throws IOException {
		if( BasePath == null ) {
			String bp = Source.getAbsolutePath();
			bp = bp.lastIndexOf( File.separator ) < 1 ? "" :
					 bp.substring( 0, bp.lastIndexOf( File.separator ) );
			BasePath = new File( bp );
		}
		;
		zip = new ZipOutputStream( new FileOutputStream( ZipFile ) );
		zip.setLevel( Level );
		zip.setComment( Comment );
		CurrentRecursion = 0;
		CompressFile( Source );
		zip.close();
	};

	protected void CompressFile( File src ) throws IOException {
		byte[] buf = new byte[BufferSize];
		if( src.exists() ) {
			if( src.isDirectory() ) {
				if( CurrentRecursion < RecursionDepth ) {
					File dir[] = src.listFiles( Filter );
					CurrentRecursion++;
					for( int i = 0; i < dir.length; i++ ) {
						CompressFile( dir[i] );
					}
					CurrentRecursion--;
				}
				;
			} else {
				try {
					String zename = src.getAbsolutePath().substring(
							BasePath.getAbsolutePath().length() + File.separator.length() );
					Progress.CurrentFile = zename;
					Progress.CurrentCompressed = 0;
					Thread.currentThread().yield();
					ZipEntry ze = new ZipEntry( zename );
					ze.setTime( src.lastModified() );
					int len;
					if( Checksum ) {
						CheckedInputStream crcin =
								new CheckedInputStream( new FileInputStream( src ), new Adler32() );
						if( !Priority ) {
							while( crcin.read( buf ) > 0 ) {
								Thread.currentThread().yield();
							}
						}
						;
						ze.setCrc( crcin.getChecksum().getValue() );
					}
					;
					zip.putNextEntry( ze );
					FileInputStream in = new FileInputStream( src );
					while( ( len = in.read( buf ) ) > 0 ) {
						zip.write( buf, 0, len );
						Progress.CurrentCompressed += len;
						Progress.TotalCompressed += len;
						if( !Priority ) {
							Thread.currentThread().yield();
						}
					}
					;
					zip.closeEntry();
					in.close();
					Progress.CurrentFile = "";
					Progress.CurrentCompressed = 0;
				} catch( IOException e ) {
					System.out.println( e.toString() );
					return;
				}
				;
			}
			;
		} else {
			throw( new FileNotFoundException() );
		}
		;
	};

};
