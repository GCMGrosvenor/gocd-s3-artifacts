package com.indix.gocd.utils.utils.zip;

import com.indix.gocd.utils.zip.IZipArchiveManager;
import com.indix.gocd.utils.zip.ZipArchiveManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ZipArchiveManagerTest {

    private IZipArchiveManager sut;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder folderToCompress = new TemporaryFolder();

    @Before
    public void TestSetup()
    {
        this.sut = new ZipArchiveManager();
    }

    @Test
    public void shouldCreateZipFile() {

        try {
            folderToCompress.newFile("myfile.txt");
            String archivePath = tempFolder.getRoot().toString().concat("/compressed.zip");
            sut.compressDirectory(folderToCompress.getRoot().toString(),archivePath);

            File file = new File(archivePath);
            assertTrue(file.exists());
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }

    }


    @Test
    public void archivedFolderWithOneFileShouldContainThatFileInTheArchive() {

        try {
            folderToCompress.newFile("myfile.txt");
            String archivePath = tempFolder.getRoot().toString().concat("/compressed.zip");
            sut.compressDirectory(folderToCompress.getRoot().toString(),archivePath);

            ZipFile zipFile = new ZipFile(archivePath);
            Enumeration zipFileEntries = zipFile.entries();
            assertTrue("Zip file has at least one entry", zipFileEntries.hasMoreElements());
            ZipEntry zipEntry = (ZipEntry)zipFileEntries.nextElement();
            assertEquals("First zip entry is called myfile.txt", "myfile.txt", zipEntry.getName());
            assertFalse("Zip file has no more entries", zipFileEntries.hasMoreElements());
            zipFile.close();
        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }

    }


    @Test
    public void shouldCreateZipFileWithDirectoryStructure() {

        try {
            folderToCompress.newFile("myfile.txt");
            File subfolder = folderToCompress.newFolder("sampleDir");
            File fileInSubfolder = new File(subfolder.getAbsolutePath().concat("/myfileInSubfolder.txt"));
            fileInSubfolder.createNewFile();

            String archivePath = tempFolder.getRoot().toString().concat("/compressed.zip");
            sut.compressDirectory(folderToCompress.getRoot().toString(),archivePath);

            ZipFile zipFile = new ZipFile(archivePath);

            assertEquals("Zip file has 3 entries", 3, zipFile.size());

            assertTrue("Zip file has an entry called myfile.txt", zipFile.getEntry("myfile.txt") != null);

            ZipEntry subfolderInZip = zipFile.getEntry("sampleDir/");
            assertTrue("Zip file has an entry for subfolder", subfolderInZip != null && subfolderInZip.isDirectory());

            assertTrue("Zip file has one more entry for file inside subfolder",
                    zipFile.getEntry("sampleDir" + File.separator + "myfileInSubfolder.txt") != null);

            zipFile.close();

        } catch (IOException ex) {
            Assert.fail(ex.getMessage());
        }

    }
}
