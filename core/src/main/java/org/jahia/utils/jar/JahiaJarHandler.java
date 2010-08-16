/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//
//
//  JahiaJarHandler (Deprecated !!! use zip utils)
//
//  NK      15.01.2001
//
//


package org.jahia.utils.jar;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.JahiaConsole;

/**
 * A Wrapper to handle some manipulations on a .jar file
 * (Deprecated !!! use zip utils)
 * @author Khue ng 
 */
public class JahiaJarHandler {

   /** The full path to the file **/
   private String m_FilePath;
   /** The JarFile object **/
   private JarFile m_JarFile;

   /**
    * Constructor
    *
    * @param (String) path, the full path to the file
    * @exception IOException
    */
   public JahiaJarHandler ( String path ) throws IOException {

      m_FilePath = path;
      File f = new File(path);
      try {

         m_JarFile = new JarFile(f);

      } catch ( IOException ioe ) {
         JahiaConsole.println("JahiaJarHandler","JahiaJarHandler IOException occurred " + f.getName() );
         throw new IOException ("JahiaJarHandler, IOException occurred ");
      } catch ( java.lang.NullPointerException e ) {
         JahiaConsole.println("JahiaJarHandler","JahiaJarHandler NullPointerException " + f.getName() );
         throw new IOException ("JahiaJarHandler, NullPointerException occurred ");
      }

      if ( m_JarFile == null ) {

         throw new IOException ("JahiaJarHandler, referred file is null");
      }

   }


   /**
    * Decompresses the file in it's current location
    *
    */
   public void unzip() throws JahiaException {

      try {

         File f = new File(m_FilePath);

         JahiaConsole.println("JahiaJarHandler"," Start Decompressing " + f.getName() );

         String parentPath = f.getParent() + File.separator;
         String path = null;

         FileInputStream fis = new FileInputStream(m_FilePath);
         BufferedInputStream bis = new BufferedInputStream(fis);
         ZipInputStream zis = new ZipInputStream(bis);
         ZipFile zf = new ZipFile(m_FilePath);
         ZipEntry ze = null;
         String zeName = null;

         try{

            while ( (ze = zis.getNextEntry()) != null ){
               zeName = ze.getName();
               path = parentPath + genPathFile(zeName);
               if ( ze.isDirectory() ){
                  File fo = new File(path);
                  fo.mkdirs();
               } else {
                  File fo = new File(path);
                  copyStream(zis,new FileOutputStream(fo));
               }
            }
         } finally {

            // Important !!!
            zf.close();
            fis.close();
            zis.close();
            bis.close();
         }


         JahiaConsole.println("JahiaJarHandler"," Decompressing " + f.getName() + " done ! ");

      } catch ( IOException ioe ) {

         JahiaConsole.println("JahiaJarHandler"," fail unzipping " + ioe.getMessage() );

         throw new JahiaException ("JahiaJarHandler", "faile processing unzip",
                                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, ioe);

      }

   }


   /**
    * Decompress the file in a gived folder
    *
    * @param (String) path
    */
   public void unzip(String path) throws JahiaException {

      try {

         File f = new File(m_FilePath);

         JahiaConsole.println("JahiaJarHandler"," Start Decompressing " + f.getName() );

         String destPath = null;

         FileInputStream fis = new FileInputStream(m_FilePath);
         BufferedInputStream bis = new BufferedInputStream(fis);
         ZipInputStream zis = new ZipInputStream(bis);
         ZipFile zf = new ZipFile(m_FilePath);
         ZipEntry ze = null;
         String zeName = null;

         try {

            while ( (ze = zis.getNextEntry()) != null ){
               zeName = ze.getName();
               destPath = path + File.separator + genPathFile(zeName);
               if ( ze.isDirectory() ){
                  File fo = new File(destPath);
                  fo.mkdirs();
                  fo = null;
               } else {
                  File fo = new File(destPath);
                  FileOutputStream fos = new FileOutputStream(fo);
                  copyStream(zis,fos);
                  fos.close();
                  fo = null;
               }
            }
         } finally {

            // Important !!!
            zf.close();
            fis.close();
            zis.close();
            bis.close();
         }

         JahiaConsole.println("JahiaJarHandler"," Decompressing " + f.getName() + " done ! ");

      } catch ( IOException ioe ) {

         JahiaConsole.println("JahiaJarHandler"," fail unzipping " + ioe.getMessage() );

         throw new JahiaException ("JahiaJarHandler", "faile processing unzip",
                                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, ioe);

      }

   }


   /**
    * Extract an entry of file type in the jar file
    * Return a File Object reference to the uncompressed file
    *
    * @param (String) entryName, the entry name
    * @return (File) fo, a File Handler to the file ( It's a temporary file )
    */
   public File extractFile( String entryName ) throws IOException {

      File tmpFile = null;


      // Create a temporary file and write the content of the file in it
      ZipEntry entry = m_JarFile.getEntry(entryName);

      if ( (entry != null) && !entry.isDirectory() ) {

         InputStream ins = m_JarFile.getInputStream(entry);

         if ( ins != null ){
            tmpFile = File.createTempFile("tmpfile","");
            if ( tmpFile == null || !tmpFile.canWrite() ){

               throw new IOException ("extractFile error creating temporary file");

            }

            copyStream(ins,new FileOutputStream(tmpFile));

         }
      } else {
         JahiaConsole.println("JahiaJarHandler", "extractFile(entry), " + entryName + " is null or a directory " );
         throw new IOException ("extractFileEntry  cannot find an entry file of name " + entryName);
      }

      return tmpFile;

   }


   /**
    * Extract an entry in a gived folder. If this entry is a directory,
    * all its contents are extracted too.
    *
    * @param (String) entryName, the name of an entry in the jar
    * @param (String) destPath, the path to the destination folder
    */
   public void extractEntry( String entryName,
                             String destPath ) throws JahiaException {

      try {

         ZipEntry entry = m_JarFile.getEntry(entryName);

         if ( entry == null ){
            StringBuffer strBuf = new StringBuffer(1024);
            strBuf.append(" extractEntry(), cannot find entry ");
            strBuf.append(entryName);
            strBuf.append(" in the jar file ");
            JahiaConsole.println("JahiaJarHandler", strBuf.toString());

            throw new JahiaException ("JahiaJarHandler", strBuf.toString(),
                                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY);

         }

         File destDir = new File(destPath);
         if ( destDir == null || !destDir.isDirectory() || !destDir.canWrite() ){

            JahiaConsole.println("JahiaJarHandler"," extractEntry(), cannot access to the destination dir ");

            throw new JahiaException ("JahiaJarHandler", " extractEntry(), cannot access to the destination dir ",
                                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY);
         }


         File f = new File(m_FilePath);

         JahiaConsole.println("JahiaJarHandler"," Start extractEntry(entryName,path) Decompressing entry " + entryName );

         String path = null;

         FileInputStream fis = new FileInputStream(m_FilePath);
         BufferedInputStream bis = new BufferedInputStream(fis);
         ZipInputStream zis = new ZipInputStream(bis);
         ZipFile zf = new ZipFile(m_FilePath);
         ZipEntry ze = null;
         String zeName = null;

         while ( (ze = zis.getNextEntry()) != null && !ze.getName().equals(entryName) ) {
            // loop until the requested entry
            JahiaConsole.println("JahiaJarHandler","extractEntry(extryName, path), bypass " + ze.getName() );
         }

         try{

            while ( ze != null ){
               zeName = ze.getName();
               path = destPath + File.separator + genPathFile(zeName);
               if ( ze.isDirectory() ){
                  File fo = new File(path);
                  fo.mkdirs();
               } else {
                  File fo = new File(path);
                  copyStream(zis,new FileOutputStream(fo));
               }

               ze = zis.getNextEntry();
            }

         } finally {

            // Important !!!
            zf.close();
            fis.close();
            zis.close();
            bis.close();
         }


         JahiaConsole.println("JahiaJarHandler"," Decompressing " + f.getName() + " done ! ");

      } catch ( IOException ioe ) {

         JahiaConsole.println("JahiaJarHandler"," fail unzipping " + ioe.getMessage() );

         throw new JahiaException ("JahiaJarHandler", "faile processing unzip",
                                    JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY, ioe);

      }

   }


   /**
    * Return an entry in the jar file of the gived name or null if not found
    *
    * @param (String) entryName the entry name
    * @return (ZipEntry) the entry
    */
   public ZipEntry getEntry(String entryName){

      return m_JarFile.getEntry(entryName);

   }


   /**
    * Check if an entry is an directory or not
    *
    * @param (String) entryName the entry name
    * @return (boolean) true if the entry exists and is a a directory
    */
   public boolean isDirectory(String entryName){
      return (( m_JarFile.getEntry(entryName) != null) && m_JarFile.getEntry(entryName).isDirectory());
   }

   /**
    * Check if an entry exist or not
    *
    * @param (String) entryName the entry name
    * @return (boolean) true if exist
    */
   public boolean entryExists(String entryName){

      if ( m_JarFile.getEntry(entryName) != null ){
         return true;
      }
      return false;

   }


   /**
    * Close the Zip file. Important to close the JarFile object
    * to be able to delete it from disk.
    *
    */
   public void closeJarFile(){

      try {
         m_JarFile.close();
      } catch ( IOException e ) {
         JahiaConsole.println("JahiaJarHandler","cannot close jar file");
         // cannot close file
      }

   }


   /**
    * Generates a path file for a gived entry name
    * Parses "/" char and replaces them with File.separator char
    *
    */
   protected String genPathFile(String entryName){

      StringBuffer sb = new StringBuffer(entryName.length());
      for ( int i= 0; i< entryName.length() ; i++ ){
         if ( entryName.charAt(i) == '/' ){
            sb.append(File.separator);
         } else {
            sb.append(entryName.charAt(i));
         }
      }

      return ( sb.toString() );

   }


	/**
    * Copy an InputStream to an OutPutStream
    *
    * @param ins An InputStream.
    * @param outs An OutputStream.
    * @exception IOException.
    */
   protected void copyStream( InputStream ins,
                            OutputStream outs)
       throws IOException
   {
      int bufferSize = 1024;
      byte[] writeBuffer = new byte[bufferSize];

      BufferedOutputStream bos =
         new BufferedOutputStream(outs, bufferSize);
      int bufferRead;
      while((bufferRead = ins.read(writeBuffer)) != -1)
         bos.write(writeBuffer,0,bufferRead);
      bos.flush();
      bos.close();
   }





} // End Class JahiaJarHandler
