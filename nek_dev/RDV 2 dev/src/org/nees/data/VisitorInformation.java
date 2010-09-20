//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.11.28 at 03:46:45 PM GMT 
//


package org.nees.data;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VisitorInformation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VisitorInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="drivingInstructionPageCount" type="{}SmallInt"/>
 *         &lt;element name="siteLocationPageCount" type="{}SmallInt"/>
 *         &lt;element name="localAreaInformationPageCount" type="{}SmallInt"/>
 *         &lt;element name="drivingInstructionFile_id" type="{}IDnumber"/>
 *         &lt;element name="siteLocationMapFile_id" type="{}IDnumber"/>
 *         &lt;element name="localAreaInformationFile_id" type="{}IDnumber"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{}IDnumber" />
 *       &lt;attribute name="link" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VisitorInformation", propOrder = {
    "drivingInstructionPageCount",
    "siteLocationPageCount",
    "localAreaInformationPageCount",
    "drivingInstructionFileId",
    "siteLocationMapFileId",
    "localAreaInformationFileId"
})
public class VisitorInformation {

    @XmlElementRef(name = "drivingInstructionPageCount", type = JAXBElement.class)
    protected JAXBElement<Integer> drivingInstructionPageCount;
    @XmlElementRef(name = "siteLocationPageCount", type = JAXBElement.class)
    protected JAXBElement<Integer> siteLocationPageCount;
    @XmlElementRef(name = "localAreaInformationPageCount", type = JAXBElement.class)
    protected JAXBElement<Integer> localAreaInformationPageCount;
    @XmlElementRef(name = "drivingInstructionFile_id", type = JAXBElement.class)
    protected JAXBElement<Integer> drivingInstructionFileId;
    @XmlElementRef(name = "siteLocationMapFile_id", type = JAXBElement.class)
    protected JAXBElement<Integer> siteLocationMapFileId;
    @XmlElementRef(name = "localAreaInformationFile_id", type = JAXBElement.class)
    protected JAXBElement<Integer> localAreaInformationFileId;
    @XmlAttribute
    protected Integer id;
    @XmlAttribute
    protected String link;

    /**
     * Gets the value of the drivingInstructionPageCount property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getDrivingInstructionPageCount() {
        return drivingInstructionPageCount;
    }

    /**
     * Sets the value of the drivingInstructionPageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setDrivingInstructionPageCount(JAXBElement<Integer> value) {
        this.drivingInstructionPageCount = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the siteLocationPageCount property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getSiteLocationPageCount() {
        return siteLocationPageCount;
    }

    /**
     * Sets the value of the siteLocationPageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setSiteLocationPageCount(JAXBElement<Integer> value) {
        this.siteLocationPageCount = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the localAreaInformationPageCount property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getLocalAreaInformationPageCount() {
        return localAreaInformationPageCount;
    }

    /**
     * Sets the value of the localAreaInformationPageCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setLocalAreaInformationPageCount(JAXBElement<Integer> value) {
        this.localAreaInformationPageCount = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the drivingInstructionFileId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getDrivingInstructionFileId() {
        return drivingInstructionFileId;
    }

    /**
     * Sets the value of the drivingInstructionFileId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setDrivingInstructionFileId(JAXBElement<Integer> value) {
        this.drivingInstructionFileId = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the siteLocationMapFileId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getSiteLocationMapFileId() {
        return siteLocationMapFileId;
    }

    /**
     * Sets the value of the siteLocationMapFileId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setSiteLocationMapFileId(JAXBElement<Integer> value) {
        this.siteLocationMapFileId = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the localAreaInformationFileId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getLocalAreaInformationFileId() {
        return localAreaInformationFileId;
    }

    /**
     * Sets the value of the localAreaInformationFileId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setLocalAreaInformationFileId(JAXBElement<Integer> value) {
        this.localAreaInformationFileId = ((JAXBElement<Integer> ) value);
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }

}