/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.explorabox.runs;

import com.github.ffremont.explorabox.ExploraBox;
import com.github.ffremont.explorabox.FolderResult;
import com.github.ffremont.explorabox.Synchronisation;
import com.github.ffremont.explorabox.models.SimFile;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.LoggerFactory;

/**
 *
 * @author florent
 */
public class ExploraCaller implements Callable<FolderResult> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExploraCaller.class);
    
    private static final String WEB_SEPARATOR = "/";
    
    private static Client exploraClient;

    private String baseuri;

    static {
        exploraClient = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(JsonProcessingFeature.class)
                .build();
    }
    private final String webPath;
    private final Synchronisation synchronisation;

    public ExploraCaller(Synchronisation sync, String baseuri, String webPath) {
        this.baseuri = baseuri;
        this.webPath = webPath;
        this.synchronisation = sync;
    }
    
    private ExploraCaller newChild(String baseuri, String webPath){
        return new ExploraCaller(this.synchronisation, baseuri, webPath);
    }

    @Override
    public FolderResult call() throws Exception {
        WebTarget target = exploraClient.target(UriBuilder.fromUri(baseuri)
                .queryParam("path", URLEncoder.encode(webPath, "UTF-8"))
                .build());

        ClientResponse response = target.request().accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if (response.getStatus() != 200) {
            return FolderResult.error("La réponse du serveur est invalide " + response.getStatus());
        } else {
            try {
                SimFile[] filesAndFolders = response.getEntity(SimFile[].class);
                
                for(SimFile file : filesAndFolders){
                    if(file.isIsDir()){
                        synchronisation.explore(newChild(baseuri, webPath+WEB_SEPARATOR+file.getFilename()));
                    }
                }
                
                return FolderResult.ok(webPath, filesAndFolders);
            } catch (ClientHandlerException | UniformInterfaceException e) {
                LOG.error("Echec de l'appel vers explora", e);
                return FolderResult.error("Appel vers Explora impossible");
            }
        }
        /**
         *
         * final Client client = ClientBuilder.newBuilder()
         * .register(MultiPartFeature.class) .build(); WebTarget t =
         * client.target(Main.BASE_URI).path("multipart").path("upload2");
         *
         * FileDataBodyPart filePart = new FileDataBodyPart("file", new
         * File("stackoverflow.png")); filePart.setContentDisposition(
         * FormDataContentDisposition.name("file")
         * .fileName("stackoverflow.png").build());
         *
         * String empPartJson = "{\n" + " \"id\": 1234,\n" + " \"name\":
         * \"Peeskillet\"\n" + "}\n" + "";
         *
         * MultiPart multipartEntity = new FormDataMultiPart() .field("emp",
         * empPartJson, MediaType.APPLICATION_JSON_TYPE) .bodyPart(filePart);
         *
         * Response response = t.request().post( Entity.entity(multipartEntity,
         * MediaType.MULTIPART_FORM_DATA));
         * System.out.println(response.getStatus());
         * System.out.println(response.readEntity(String.class));
         *
         * response.close();
         */
    }

}
