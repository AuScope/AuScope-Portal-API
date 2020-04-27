<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" xmlns="http://www.opengis.net/sld" xmlns:public="http://capdf.csiro.au/" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:ows="http://www.opengis.net/ows" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
        <Name>[name]</Name>
        <UserStyle>
            <Title>default Title</Title>
            <Abstract>default abstract</Abstract>
            <FeatureTypeStyle>
                <Rule>
                    <Name>Hydrogeo Chemistry</Name>
                    <Title>Hydrogeo Chemistry</Title>
                    <Abstract>Light purple square boxes</Abstract>
                    [stylefilterRules]
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