<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:erl="http://xmlns.earthresourceml.org/earthresourceml-lite/1.0" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:ows="http://www.opengis.net/ows" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
        <Name>[name]</Name>
        <UserStyle>
            <Title>Erml Lite</Title>
            <Abstract>Erml Lite</Abstract>
            <FeatureTypeStyle>
                <Rule>
                    <Name>ERL View</Name>
                    <Title>ERL View</Title>
                    <Abstract>Circle</Abstract>
                    [filter]
                    <PointSymbolizer>
                        <Graphic>
                            <Mark>
                                <WellKnownName>circle</WellKnownName>
                                <Fill>
                                    <CssParameter name="fill">[color]</CssParameter>
                                    <CssParameter name="fill-opacity">0.4</CssParameter>
                                </Fill>
                                <Stroke>
					               <CssParameter name="stroke">[color]</CssParameter>
					               <CssParameter name="stroke-width">1</CssParameter>
					             </Stroke>
                            </Mark>
                            <Size>8</Size>
                        </Graphic>
                    </PointSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>