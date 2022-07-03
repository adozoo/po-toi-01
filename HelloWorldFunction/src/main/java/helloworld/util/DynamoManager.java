package helloworld.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DynamoManager {

    private static final AWSCredentialsProvider credentialsProvider =
            new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(DynamoConstants.ACCESS_KEY, DynamoConstants.ACCESS_SECRET));

    private static final AmazonDynamoDB dynamoDB =
            AmazonDynamoDBClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain())
            .build();

    private static final DynamoDB dynamo = new DynamoDB(dynamoDB);

    private static final Table table = dynamo.getTable(DynamoConstants.TABLE_NAME);

    public static Table getTable() {
        return table;
    }

    public static AmazonDynamoDB getDynamoDB() {
        return dynamoDB;
    }

    public static DynamoDB getDynamo() {
        return dynamo;
    }
}
