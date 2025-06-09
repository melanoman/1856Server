package mel.volvox.GameChatServer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entity class represents a table in a relational database
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "human", schema = "test")
public class Human {
    @Id
    String accountName;
    String displayName;
    String pass;// TODO Encrypt password
    String contact;
}