package com.smartHome.webapp.model;


import java.io.Serializable;
import java.util.Objects;

/** SensorsResponseEntity */

@javax.annotation.Generated(
  value = "io.swagger.codegen.languages.SpringCodegen",
  date = "2018-04-03T15:24:02.669+02:00"
)
public class SensorsResponseEntity implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


private Integer id = null;
  

  private DataResponse data = null;
  
  public SensorsResponseEntity dataResponse(DataResponse data) {
	    this.data  = data;
	    return this;
	  }
  
  /**
   * Get dataResponse
   *
   * @return data
   */

  public DataResponse getDataResponse() {
    return data;
  }

  public void setDataResponse(DataResponse data) {
    this.data = data;
  }
  

  public SensorsResponseEntity id(Integer id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   *
   * @return id
   */

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }
  


	  

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SensorsResponseEntity SensorsResponseEntityReply = (SensorsResponseEntity) o;
    return Objects.equals(this.id, SensorsResponseEntityReply.id);
  }



  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SensorsResponseEntityReply {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");

    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
