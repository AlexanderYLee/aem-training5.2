package com.mycompany.myproject.impl.servlets;

import com.day.cq.commons.ImageHelper;
import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.commons.AbstractImageServlet;
import com.day.cq.wcm.foundation.Image;
import com.day.image.Layer;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 *
 * Created by Aliaksandr_Li on 5/3/2016.
 */

@SlingServlet(resourceTypes = {"sling/servlet/default"}, selectors = {"ud"}, extensions = {"jpg", "png"})
public class UpsideDownImageServlet extends SlingSafeMethodsServlet{
    private Logger logger = LoggerFactory.getLogger(UpsideDownImageServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        logger.debug("UpsideDownImageServlet called!");
        Image image = null;
        if (ResourceUtil.isNonExistingResource(request.getResource())){
            Resource resource = request.getResourceResolver().
                    getResource(request.getResource().getPath().replaceAll("\\.ud", ""));
            if (ResourceUtil.isNonExistingResource(resource)){
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }else{
                image = new Image(resource);
                image.setFileReference(image.getPath());
            }
        }else{
            AbstractImageServlet.ImageContext imageContext = new AbstractImageServlet.ImageContext(request,
                    request.getContentType());
            image = new Image(imageContext.resource);
            if (!image.hasContent()) {
                if (imageContext.defaultResource != null) {
                    image = new Image(imageContext.defaultResource);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
            image.loadStyleData(imageContext.style);
        }


        Layer layer = null;
        try {
            layer = image.getLayer(false, false, false);
            if (layer != null) {
                layer.rotate(180d);
            }
            String mimeType = image.getMimeType();
            if (ImageHelper.getExtensionFromType(mimeType) == null) {
                mimeType = "image/png";
            }
            response.setContentType(mimeType);
            layer.write(mimeType, 1.0, response.getOutputStream());
            response.flushBuffer();
        } catch (RepositoryException e) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }
    }
}
