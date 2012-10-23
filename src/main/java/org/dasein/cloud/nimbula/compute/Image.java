/**
 * Copyright (C) 2009-2012 enStratus Networks Inc
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.nimbula.compute;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.dasein.cloud.AsynchronousTask;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageFormat;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.MachineImageType;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.nimbula.NimbulaDirector;
import org.dasein.cloud.nimbula.NimbulaMethod;
import org.dasein.util.Jiterator;
import org.dasein.util.JiteratorPopulator;
import org.dasein.util.PopulatorThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;

public class Image implements MachineImageSupport {
    static private final Logger logger = NimbulaDirector.getLogger(Image.class);
    
    static public final String IMAGELIST    = "imagelist";
    static public final String MACHINEIMAGE = "machineimage";
    
    private NimbulaDirector cloud;
    
    Image(@Nonnull NimbulaDirector cloud) { this.cloud = cloud; }

    @Override
    public void downloadImage(String machineImageId, OutputStream toOutput) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public MachineImage getMachineImage(String machineImageId) throws CloudException, InternalException {
        NimbulaMethod method = new NimbulaMethod(cloud, MACHINEIMAGE);
        
        try {
            int code = method.get(machineImageId);
            
            if( code == 404 || code == 401 ) {
                return null;
            }
        }
        catch( HttpException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        catch( IOException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        try {
            return toMachineImage(method.getResponseBody());
        }
        catch( JSONException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error parsing JSON: " + e.getMessage());
                e.printStackTrace();
            }
            throw new InternalException(e);
        } 
    }

    public String getMachineImageId(String imagelist, int entryNumber) throws CloudException, InternalException {
        NimbulaMethod method = new NimbulaMethod(cloud, Image.IMAGELIST);
        
        try {
            method.get(imagelist);
        }
        catch( HttpException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        catch( IOException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        try {
            JSONObject item = method.getResponseBody();
            JSONArray entries = item.getJSONArray("entries");

            for( int i=0; i<entries.length(); i++ ) {
                JSONObject entry = entries.getJSONObject(i);
                JSONArray images = entry.getJSONArray("machineimages");

                if( images.length() >= entryNumber ) {
                    return images.getString(entryNumber-1);
                }
            }
            return null;
        }
        catch( JSONException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error parsing JSON: " + e.getMessage());
                e.printStackTrace();
            }
            throw new InternalException(e);
        }        
    }
    
    @Override
    public String getProviderTermForImage(Locale locale) {
        return "machine image";
    }

    @Override
    public boolean hasPublicLibrary() {
        return true;
    }

    @Override
    public AsynchronousTask<String> imageVirtualMachine(String vmId, String name, String description) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AsynchronousTask<String> imageVirtualMachineToStorage(String vmId, String name, String description, String directory) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isImageSharedWithPublic(String machineImageId) throws CloudException, InternalException {
        return machineImageId.startsWith("/nimbula/public");
    }

    @Override
    public String installImageFromUpload(MachineImageFormat format, InputStream imageStream) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return true;
    }

    @Override
    public Iterable<MachineImage> listMachineImages() throws CloudException, InternalException {
        NimbulaMethod method = new NimbulaMethod(cloud, MACHINEIMAGE);
        
        try {
            method.list();
        }
        catch( HttpException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        catch( IOException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        try {
            ArrayList<MachineImage> images = new ArrayList<MachineImage>();
            JSONArray array = method.getResponseBody().getJSONArray("result");
            
            for( int i=0; i<array.length(); i++ ) {
                MachineImage image = toMachineImage(array.getJSONObject(i));
                
                if( image != null ) {
                    images.add(image);
                }
            }
            return images;
        }
        catch( JSONException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error parsing JSON: " + e.getMessage());
                e.printStackTrace();
            }
            throw new InternalException(e);
        } 
    }

    @Override
    public Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
        if( accountId == null ) {
            accountId =  "/nimbula/public/";
        }
        else if( !accountId.endsWith("/") ){
            accountId = accountId + "/";
        }
        NimbulaMethod method = new NimbulaMethod(cloud, MACHINEIMAGE);
        
        try {
            int code = method.get(accountId);
            
            if( code == 401 ) {
                return Collections.emptyList();
            }
        }
        catch( HttpException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        catch( IOException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        try {
            ArrayList<MachineImage> images = new ArrayList<MachineImage>();
            JSONArray array = method.getResponseBody().getJSONArray("result");
            
            for( int i=0; i<array.length(); i++ ) {
                MachineImage image = toMachineImage(array.getJSONObject(i));
                
                if( image != null ) {
                    images.add(image);
                }
            }
            return images;
        }
        catch( JSONException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error parsing JSON: " + e.getMessage());
                e.printStackTrace();
            }
            throw new InternalException(e);
        } 
    }

    @Override
    public Iterable<String> listShares(String forMachineImageId) throws CloudException, InternalException {
        return Collections.emptyList();
    }

    @Override
    public Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
        return Collections.singletonList(MachineImageFormat.NIMBULA);
    }

    @Override
    public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
        return new String[0];
    }

    private boolean matches(MachineImage image, String keyword, Platform platform, Architecture architecture) {
        if( architecture != null && !architecture.equals(image.getArchitecture()) ) {
            return false;
        }
        if( platform != null && !platform.equals(Platform.UNKNOWN) ) {
            Platform mine = image.getPlatform();
            
            if( platform.isWindows() && !mine.isWindows() ) {
                return false;
            }
            if( platform.isUnix() && !mine.isUnix() ) {
                return false;
            }
            if( platform.isBsd() && !mine.isBsd() ) {
                return false;
            }
            if( platform.isLinux() && !mine.isLinux() ) {
                return false;
            }
            if( platform.equals(Platform.UNIX) ) {
                if( !mine.isUnix() ) {
                    return false;
                }
            }
            else if( !platform.equals(mine) ) {
                return false;
            }
        }
        if( keyword != null ) {
            keyword = keyword.toLowerCase();
            if( !image.getDescription().toLowerCase().contains(keyword) ) {
                if( !image.getName().toLowerCase().contains(keyword) ) {
                    if( !image.getProviderMachineImageId().toLowerCase().contains(keyword) ) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    public String registerMachineImage(String atStorageLocation) throws CloudException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(String machineImageId) throws CloudException, InternalException {
        NimbulaMethod method = new NimbulaMethod(cloud, MACHINEIMAGE);
        
        try {
            method.delete(machineImageId);
        }
        catch( HttpException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
        catch( IOException e ) {
            if( logger.isDebugEnabled() ) {
                logger.error("Error in API call: " + e.getMessage());
                e.printStackTrace();
            }
            throw new CloudException(e);
        }
    }

    @Override
    public Iterable<MachineImage> searchMachineImages(String keyword, Platform platform, Architecture architecture) throws CloudException, InternalException {
        PopulatorThread<MachineImage> populator;
        final String k = keyword;
        final Platform p = platform;
        final Architecture a = architecture;
        
        cloud.hold();
        populator = new PopulatorThread<MachineImage>(new JiteratorPopulator<MachineImage>() {
            @Override
            public void populate(Jiterator<MachineImage> iterator) throws Exception {
                try {
                    for( MachineImage image : listMachineImages() ) {
                        if( matches(image, k, p, a) ) {
                            iterator.push(image);
                        }
                    }
                    for( MachineImage image : listMachineImagesOwnedBy(null) ) {
                        if( matches(image, k, p, a) ) {
                            iterator.push(image);
                        }
                    } 
                }
                finally {
                    cloud.release();
                }
            }            
        });
        populator.populate();
        return populator.getResult();
    }

    @Override
    public void shareMachineImage(String machineImageId, String withAccountId, boolean allow) throws CloudException, InternalException {
        throw new OperationNotSupportedException("Nimbula does not support image sharing of any kind.");
    }

    @Override
    public boolean supportsCustomImages() {
        return false;
    }

    @Override
    public boolean supportsImageSharing() {
        return false;
    }

    @Override
    public boolean supportsImageSharingWithPublic() {
        return false;
    }

    private MachineImage toMachineImage(JSONObject ob) throws JSONException {
        MachineImage image = new MachineImage();
        String name = ob.getString("name");
        String[] idInfo = cloud.parseId(name);
        
        image.setProviderOwnerId(idInfo[0]);
        image.setProviderMachineImageId(name);
        image.setName(idInfo[2]);
        Platform platform = Platform.UNKNOWN;
        try {
            JSONObject attrs = ob.getJSONObject("attributes");
            
            platform = Platform.guess(attrs.getString("type"));
            image.setDescription(idInfo[2] + " (" + attrs.getString("type") + ")");
        }
        catch( Throwable ignore ) {
            image.setDescription(idInfo[2]);
        }
        image.setPlatform(platform);
        image.setArchitecture(Architecture.I64);
        image.setCurrentState(MachineImageState.ACTIVE);
        image.setProviderRegionId(cloud.getContext().getRegionId());
        image.setSoftware("");
        image.setType(MachineImageType.STORAGE);
        return image;
    }
    
    @Override
    public String transfer(CloudProvider fromCloud, String machineImageId) throws CloudException, InternalException {
        return null;
    }

}