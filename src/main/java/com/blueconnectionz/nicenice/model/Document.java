package com.blueconnectionz.nicenice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@ToString
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String url;
    private String uniqueDocumentId;

    public Document(String url, String uniqueDocumentId) {
        this.url = url;
        this.uniqueDocumentId = uniqueDocumentId;
    }
}

        /*
        spring.datasource.url= jdbc:mysql://nicenicev1db.cxh6kky3suqx.ap-northeast-1.rds.amazonaws.com:3306/nicenicev1db
        spring.datasource.username= niceniceadmin
        spring.datasource.password= 12345678
        */
