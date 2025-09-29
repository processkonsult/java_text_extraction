package com.workflow.textExtraction;

import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class TextExtractionBeanInfo extends SimpleBeanInfo {
    /* BeanInfo classes are used to provide more user-friendly descriptions for Java integration parameters via the IBM BAW designer environment */

    private Class beanClass = TextExtraction.class; // The class this BeanInfo refers to
    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        try {
			ArrayList<MethodDescriptor> methodDescriptors = new ArrayList<MethodDescriptor>();

			// Describe the Methods and add directly into a list
			methodDescriptors.add(getMethodDescription(
					"extractPdfTextFromFilePath",
                    new String[] { "filePath", "appendExtractedFields" },
                    new Class[] { String.class, Boolean.class }));

			methodDescriptors.add(getMethodDescription(
					"extractPdfTextFromB64",
                    new String[] { "base64String", "appendExtractedFields" },
                    new Class[] { String.class, Boolean.class }));

			methodDescriptors.add(getMethodDescription(
					"ocrPdfFromB64",
                    new String[] { "base64String" },
                    new Class[] { String.class }));

			methodDescriptors.add(getMethodDescription(
					"ocrImageFromB64",
                    new String[] { "base64String", "imageExtension" },
                    new Class[] { String.class, String.class }));

			methodDescriptors.add(getMethodDescription(
					"unzip",
                    new String[] { "zipFilePath", "unzipFolderPath" },
                    new Class[] { String.class, String.class }));

			methodDescriptors.add(getMethodDescription(
					"fileExists",
                    new String[] { "filePath" },
                    new Class[] { String.class }));

			methodDescriptors.add(getMethodDescription(
					"deleteFile",
                    new String[] { "filePath" },
                    new Class[] { String.class }));

			methodDescriptors.add(getMethodDescription(
					"getTess4jFolderPath",
                    null,
                    null));
			
			methodDescriptors.add(getMethodDescription(
					"extractTextFromB64_Tika",
                    new String[] { "base64String" },
                    new Class[] { String.class }));
			
			methodDescriptors.add(getMethodDescription(
					"getPdfPageCount",
                    new String[] { "base64String" },
                    new Class[] { String.class }));

			methodDescriptors.add(getMethodDescription(
					"convertPdfToB64ImageOcrArray",
                    new String[] { "base64String" },
                    new Class[] { String.class }));
			
			return methodDescriptors.toArray(new MethodDescriptor[0]);
			
        } catch (Exception e) {
            return super.getMethodDescriptors();
        }
    }

    // Build Method descriptor
    private MethodDescriptor getMethodDescription(String methodName, String parameters[], Class classes[]) throws NoSuchMethodException {
        MethodDescriptor methodDescriptor = null;
        Method method = beanClass.getMethod(methodName, classes);

        if (method != null) {
            if(parameters != null) {
	        	ParameterDescriptor paramDescriptors[] = new ParameterDescriptor[parameters.length];
	            for (int i = 0; i < parameters.length; i++) {
	                ParameterDescriptor param = new ParameterDescriptor();
	                param.setShortDescription(parameters[i]);
	                param.setDisplayName(parameters[i]);
	                paramDescriptors[i] = param;
	            }
	            methodDescriptor = new MethodDescriptor(method, paramDescriptors);
            } else {
                methodDescriptor = new MethodDescriptor(method);
            }
        }

        return methodDescriptor;
    }

}

