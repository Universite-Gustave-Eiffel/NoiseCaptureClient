package org.noiseplanet.noisecapture.mock

import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import org.noiseplanet.noisecapture.services.storage.CacheFileSystemService
import org.noiseplanet.noisecapture.services.storage.FileSystemService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class MockFileSystemService : FileSystemService, CacheFileSystemService {

    // - Properties

    private val storage: MutableMap<Path, ByteArray> = mutableMapOf()


    // - Public functions

    override suspend fun read(fileUri: String): ByteArray? {
        val path = getAbsolutePath(fileUri) ?: return null

        return storage[path]
    }

    override suspend fun write(fileUri: String, bytes: ByteArray, append: Boolean) {
        val path = getAbsolutePath(fileUri) ?: return

        if (append) {
            storage[path]?.let { existing ->
                storage[path] = existing + bytes
            }
        } else {
            storage[path] = bytes
        }
    }

    override suspend fun size(fileUri: String): Long? {
        val path = getAbsolutePath(fileUri) ?: return null

        return storage[path]?.size?.toLong()
    }

    override suspend fun delete(fileUri: String) {
        val path = getAbsolutePath(fileUri) ?: return

        storage.remove(path)
    }

    override suspend fun exists(fileUri: String): Boolean {
        val path = getAbsolutePath(fileUri) ?: return false

        return storage.contains(path)
    }

    override suspend fun list(uri: String): List<String> {
        val path = getAbsolutePath(uri) ?: return emptyList()

        return storage.keys.map { it.toString() }
            .filter { it.startsWith(path.toString()) }
            .map { it.removePrefix(getRootDirectory()) }
    }

    // Does nothing
    override suspend fun download(fileUri: String) = Unit

    // Does nothing
    override suspend fun download(fileUris: List<String>, archiveName: String) = Unit

    override fun getRootDirectory(): String = "root/"
}


class MockFileSystemServiceTests {

    @Test
    fun testAbsolutePaths() = runTest {
        val service = MockFileSystemService()

        assertEquals(
            Path("${service.getRootDirectory()}/testFile.json"),
            service.getAbsolutePath("/testFile.json")
        )
    }

    @Test
    fun testReadWrite() = runTest {
        val service = MockFileSystemService()
        val fileUri = "dir/file.txt"
        val fileContents = "contents of file."

        assertNull(service.read(fileUri))

        service.writeString(fileUri, fileContents)
        assertEquals(fileContents, service.readString(fileUri))

        service.writeString(fileUri, fileContents, append = true)
        assertEquals(fileContents + fileContents, service.readString(fileUri))

        service.writeString(fileUri, fileContents)
        assertEquals(fileContents, service.readString(fileUri))
    }

    @Test
    fun testWriteDelete() = runTest {
        val service = MockFileSystemService()
        val fileUri = "dir/file.txt"
        val fileContents = "contents of file."

        assertNull(service.read(fileUri))
        assertNull(service.size(fileUri))
        assertFalse(service.exists(fileUri))

        service.writeString(fileUri, fileContents)
        assertNotNull(service.readString(fileUri))
        assertNotNull(service.size(fileUri))
        assertTrue(service.exists(fileUri))

        service.delete(fileUri)
        assertNull(service.read(fileUri))
        assertNull(service.size(fileUri))
        assertFalse(service.exists(fileUri))
    }

    @Test
    fun testList() = runTest {
        val service = MockFileSystemService()
        val fileContents = "contents of file."

        assertEquals(emptyList(), service.list())

        service.writeString("dir1/subdir1/file1.txt", fileContents)
        service.writeString("dir1/subdir1/file2.txt", fileContents)
        service.writeString("dir1/subdir1/file3.txt", fileContents)

        service.writeString("dir1/subdir2/file1.txt", fileContents)
        service.writeString("dir1/subdir2/file2.txt", fileContents)

        service.writeString("dir2/subdir1/file1.txt", fileContents)

        assertEquals(
            listOf(
                "dir1/subdir1/file1.txt",
                "dir1/subdir1/file2.txt",
                "dir1/subdir1/file3.txt",
                "dir1/subdir2/file1.txt",
                "dir1/subdir2/file2.txt",
            ),
            service.list("dir1/")
        )

        assertEquals(
            listOf(
                "dir1/subdir2/file1.txt",
                "dir1/subdir2/file2.txt",
            ),
            service.list("dir1/subdir2")
        )

        assertEquals(
            listOf(
                "dir1/subdir1/file1.txt",
                "dir1/subdir1/file2.txt",
                "dir1/subdir1/file3.txt",
                "dir1/subdir2/file1.txt",
                "dir1/subdir2/file2.txt",
                "dir2/subdir1/file1.txt",
            ),
            service.list("")
        )
    }
}
