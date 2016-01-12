package com.indix.gocd.s3fetch;

import com.amazonaws.services.s3.AmazonS3Client;
import com.indix.gocd.utils.AWSCredentialsFactory;
import com.indix.gocd.utils.store.S3ArtifactStore;
import com.indix.gocd.utils.zip.IZipArchiveManager;
import com.indix.gocd.utils.zip.ZipArchiveManager;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FetchExecutor implements TaskExecutor {
    private static Logger logger = Logger.getLoggerFor(FetchTask.class);
    private IZipArchiveManager zipArchiveManager = getZipArchiveManager();

    @Override
    public ExecutionResult execute(TaskConfig config, final TaskExecutionContext context) {
        final FetchConfig fetchConfig = new FetchConfig(config, context);

        ValidationResult validationResult = fetchConfig.validate();
        if(!validationResult.isSuccessful()) {
            return ExecutionResult.failure(validationResult.getMessages().toString());
        }
        final AWSCredentialsFactory factory = new AWSCredentialsFactory(fetchConfig.asMap());

        final S3ArtifactStore store = s3ArtifactStore(fetchConfig, factory);

        String artifactPathOnS3 = fetchConfig.getArtifactsLocationTemplate();
        context.console().printLine(String.format("Getting artifacts from %s", store.pathString(artifactPathOnS3)));
        String destination = String.format("%s/%s", context.workingDir(), config.getValue(FetchTask.DESTINATION));
        setupDestinationDirectory(destination);

        try {
            store.getPrefix(artifactPathOnS3, destination);
        } catch (Exception e) {
            String message = String.format("Failure while downloading artifacts - %s", e.getMessage());
            logger.error(message, e);
            return ExecutionResult.failure(message, e);
        }
        List<File> files = (List<File>)FileUtils.listFiles(new File(destination), new String[] {"zip"}, true);
        for(File zipFile:files) {
            if (zipFile.getName().endsWith("artifacts.zip")) {
                try {
                    logger.debug(String.format("Artifact is archive.zip: un-compressing %s into %s",
                            zipFile.getAbsolutePath(), zipFile.getParent()));
                    zipArchiveManager.extractArchive(zipFile.getAbsolutePath(), zipFile.getParent());
                } catch (IOException e) {
                    String message = String.format("Error during un-compressing archive: %s", e.getMessage());
                    logger.error(message);
                    return ExecutionResult.failure(message, e);
                }
                CleanUpZip(zipFile);
            }
        }

        return ExecutionResult.success("Fetched all artifacts");
    }

    private void CleanUpZip(File zipFile) {
        try {
            zipFile.delete();
        } catch (RuntimeException e)
        {
            logger.warn(String.format("Could not delete zip file %s", zipFile.getAbsolutePath()));
        }
    }
    private void setupDestinationDirectory(String destination) {
        File destinationDirectory = new File(destination);
        try {
            if(destinationDirectory.exists()) {
                FileUtils.cleanDirectory(destinationDirectory);
                FileUtils.deleteDirectory(destinationDirectory);
            }
            FileUtils.forceMkdir(destinationDirectory);
        } catch (IOException ioe) {
            logger.error(String.format("Error while setting up destination - %s", ioe.getMessage()), ioe);
        }
    }

    public S3ArtifactStore s3ArtifactStore(FetchConfig config, AWSCredentialsFactory factory) {
        return new S3ArtifactStore(s3Client(factory), config.getS3Bucket());
    }

    public AmazonS3Client s3Client(AWSCredentialsFactory factory) {
        return new AmazonS3Client(factory.getCredentialsProvider());
    }

    public IZipArchiveManager getZipArchiveManager() {
        return new ZipArchiveManager();
    }
}

