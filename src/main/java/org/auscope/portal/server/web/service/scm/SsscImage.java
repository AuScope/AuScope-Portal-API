package org.auscope.portal.server.web.service.scm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SsscImage
{
  private String imageId;
  private String provider;
  private String command;
  private Set<String> annotations;

  @JsonProperty("image_id")
  public String getImageId() { return imageId; }
  public void setImageId(String imageId) { this.imageId = imageId; }

  public String getProvider() { return provider; }
  public void setProvider(String provider) { this.provider = provider; }

  public String getCommand() { return command; }
  public void setComment(String command) { this.command = command; }

  public Set<String> getAnnotations() { return annotations; }
  public void setAnnotations(Collection<String> annotations) {
    if (this.annotations == null) {
      this.annotations = new HashSet<String>();
    } else {
      this.annotations.clear();
    }

    this.annotations.addAll(annotations);
  }
}
