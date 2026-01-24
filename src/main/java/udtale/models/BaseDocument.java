package udtale.models;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class BaseDocument {
    @Id
    @Field(value = "_id", targetType = FieldType.OBJECT_ID )
    @Indexed
    protected String id;

    @Version
    protected Long version;

    @CreatedDate
    @Indexed
    @Field("_created")
    protected LocalDateTime created;

    @Indexed
    @LastModifiedDate
    @Field("_modified")
    protected LocalDateTime modified;

    @Transient
    public ObjectId oid() {
        return new ObjectId(this.id);
    }

}
