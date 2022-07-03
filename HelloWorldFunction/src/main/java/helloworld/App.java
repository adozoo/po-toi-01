package helloworld;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.regions.Regions;
import helloworld.util.DynamoConstants;


/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final AWSCredentialsProvider credentialsProvider =
            new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(DynamoConstants.ACCESS_KEY, DynamoConstants.ACCESS_SECRET));

    private static AmazonDynamoDB amazonDynamoDBClient = null;
    private static DynamoDBMapper dbMapper = null;
    private static Table table = null;

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        String paramsFromForm = input.getBody();

        String[] lines = paramsFromForm.split("\\n");

        amazonDynamoDBClient = AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider).withRegion(Regions.AP_NORTHEAST_1).build();
        dbMapper = new DynamoDBMapper(amazonDynamoDBClient);
        table = new DynamoDB(amazonDynamoDBClient).getTable(DynamoConstants.TABLE_NAME);

        String so = "";
        int sa = 0;
        String dt = "";

        String line = "";
        for (int i = 1; i < lines.length; i++) {
            line = lines[i];
            if (line.contains("form-data; name=\"janCode\"")) {
                if(lines[i + 1].trim().contains("Content-Type")){
                    so = lines[i + 4].trim();
                }else {
                    so = lines[i + 2].trim();
                }
            } else if (line.contains("form-data; name=\"rank\"")) {
                if(lines[i + 1].trim().contains("Content-Type")){
                    sa = Integer.parseInt(lines[i + 4].trim());
                }else {
                    sa = Integer.parseInt(lines[i + 2].trim());
                }
            } else if (line.contains("form-data; name=\"dt\"")) {
                if(lines[i + 1].trim().contains("Content-Type")){
                    dt = lines[i + 4].trim();
                }else {
                    dt = lines[i + 2].trim();
                }
            }
        }

        if (lines.length <= 1) {
            so = "123456789012";
            sa = 3;
            dt = "20220404";
        }

        Item item = table.getItem(DynamoConstants.PARTITION_KEY, so,
                DynamoConstants.SORT_KEY, sa);
        String flag = item.get("valid").toString();

        String base_point = item.get("point").toString();
        String base_price = item.get("price").toString();
        String base_janCode = item.get("janCode").toString();
        String campaign_point = item.get("campCode").toString();

        StringBuilder str=new StringBuilder();

        str.append("{ \"janCode\": \"");
        str.append(base_janCode);
        str.append("\", \"point \": \"");
        str.append(base_point);
        str.append("\", \"price \": \"");
        str.append(base_price);
        str.append("\", \"campaignCode \": \"");
        str.append(campaign_point);
        str.append("\" }");

        String requestItems = str.toString();

        try {
            if (!requestItems.isEmpty() && flag.equals("1")) {
                return response
                        .withStatusCode(200).withBody(requestItems);
//                        .withBody(requestItems+System.getProperty("line.separator")+paramsFromForm);
            } else {

                return response
                        .withStatusCode(200)
                        .withBody(paramsFromForm);
            }
        } catch (Exception e) {
            return response
                    .withBody(paramsFromForm)
                    .withStatusCode(500);
        }

    }
}
