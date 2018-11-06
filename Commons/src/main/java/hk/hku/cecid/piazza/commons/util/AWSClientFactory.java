package hk.hku.cecid.piazza.commons.util;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

/**
 * Created by aaronwalker on 6/11/18.
 */

public class AWSClientFactory {

    public static AmazonSQS getAmazonSQSClient(String region) {
        return AmazonSQSClientBuilder.standard().withRegion(region).build();
    }

    public static AmazonSNS getAmazonSNSClient(String region) {
        return  AmazonSNSClientBuilder.standard().withRegion(region).build();
    }

    public static AmazonS3 getAmazonS3Client(String region) {
        return AmazonS3ClientBuilder.standard().withRegion(region).build();
    }

    public static AmazonSimpleEmailService getAmazonSESClient(String region) {
        return AmazonSimpleEmailServiceClientBuilder.standard().withRegion(region).build();
    }

    public static AmazonCloudWatch getAmazonCloudWatchClient(String region) {
        return AmazonCloudWatchClientBuilder.standard().withRegion(region).build();
    }

    public static AmazonDynamoDB getAmazonDynamoDBClient(String region) {
        return AmazonDynamoDBClientBuilder.standard().withRegion(region).build();
    }

    public static AmazonKinesis getAmazonKinesisClient(String region) {
        return AmazonKinesisClientBuilder.standard().withRegion(region).build();
    }

}
