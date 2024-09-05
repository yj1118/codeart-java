package apros.codeart.ddd.metadata;

import apros.codeart.dto.DTObject;

public interface IMetadataSchemeFactory {
    Iterable<DTObject> getSchemes();
}
