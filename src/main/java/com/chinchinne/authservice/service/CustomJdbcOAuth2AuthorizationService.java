package com.chinchinne.authservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class CustomJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService
{
    private static Map<String, ColumnMetadata> columnMetadataMap;

    private final JdbcOperations jdbcOperations;
    private final LobHandler lobHandler;

    private RowMapper<OAuth2Authorization> authorizationRowMapper;
    private Function<OAuth2Authorization, List<SqlParameterValue>> authorizationParametersMapper;

    public CustomJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository)
    {
        this(jdbcOperations, registeredClientRepository, new DefaultLobHandler());
    }

    public CustomJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository, LobHandler lobHandler)
    {
        super(jdbcOperations, registeredClientRepository, lobHandler);

        Assert.notNull(jdbcOperations, "jdbcOperations cannot be null");
        Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
        Assert.notNull(lobHandler, "lobHandler cannot be null");
        this.jdbcOperations = jdbcOperations;
        this.lobHandler = lobHandler;
        OAuth2AuthorizationRowMapper authorizationRowMapper = new OAuth2AuthorizationRowMapper(registeredClientRepository);
        authorizationRowMapper.setLobHandler(lobHandler);
        this.authorizationRowMapper = authorizationRowMapper;
        this.authorizationParametersMapper = new OAuth2AuthorizationParametersMapper();
        initColumnMetadata(jdbcOperations);
    }

    @Override
    public void save(OAuth2Authorization authorization)
    {
        Assert.notNull(authorization, "authorization cannot be null");
        OAuth2Authorization existingAuthorization = this.findById(authorization.getId());

        if (existingAuthorization == null)
        {
            this.insertAuthorization(authorization);
        }
        else
        {
            this.updateAuthorization(authorization);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization)
    {
        super.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id)
    {
        return super.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType)
    {
        Assert.hasText(token, "token cannot be empty");
        List<SqlParameterValue> parameters = new ArrayList();

        if (tokenType == null)
        {
            parameters.add(new SqlParameterValue(12, token));
            parameters.add(mapToSqlParameter("authorization_code_value", token));
            parameters.add(mapToSqlParameter("access_token_value", token));
            parameters.add(mapToSqlParameter("refresh_token_value", token));
            return this.findBy("state = ? OR authorization_code_value = ? OR access_token_value = ? OR refresh_token_value = ?", parameters);
        }
        else if ("state".equals(tokenType.getValue()))
        {
            parameters.add(new SqlParameterValue(12, token));
            return this.findBy("state = ?", parameters);
        }
        else if ("code".equals(tokenType.getValue()))
        {
            parameters.add(mapToSqlParameter("authorization_code_value", token));
            return this.findBy("authorization_code_value = ?", parameters);
        }
        else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType))
        {
            parameters.add(mapToSqlParameter("access_token_value", token));
            return this.findBy("access_token_value = ?", parameters);
        }
        else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType))
        {
            parameters.add(mapToSqlParameter("refresh_token_value", token));
            return this.findBy("refresh_token_value = ?", parameters);
        }
        else
        {
            return null;
        }
    }

    private void updateAuthorization(OAuth2Authorization authorization)
    {
        List<SqlParameterValue> parameters = (List)this.authorizationParametersMapper.apply(authorization);
        SqlParameterValue id = (SqlParameterValue)parameters.remove(0);
        parameters.add(id);
        LobCreator lobCreator = this.lobHandler.getLobCreator();

        try
        {
            PreparedStatementSetter pss = new LobCreatorArgumentPreparedStatementSetter(lobCreator, parameters.toArray());
            this.jdbcOperations.update("UPDATE oauth2_authorization SET registered_client_id = ?, principal_name = ?, authorization_grant_type = ?, attributes = ?, state = ?, authorization_code_value = ?, authorization_code_issued_at = ?, authorization_code_expires_at = ?, authorization_code_metadata = ?, access_token_value = ?, access_token_issued_at = ?, access_token_expires_at = ?, access_token_metadata = ?, access_token_type = ?, access_token_scopes = ?, oidc_id_token_value = ?, oidc_id_token_issued_at = ?, oidc_id_token_expires_at = ?, oidc_id_token_metadata = ?, refresh_token_value = ?, refresh_token_issued_at = ?, refresh_token_expires_at = ?, refresh_token_metadata = ? WHERE id = ?", pss);
        }
        catch (Throwable var8)
        {
            if (lobCreator != null)
            {
                try
                {
                    lobCreator.close();
                }
                catch (Throwable var7)
                {
                    var8.addSuppressed(var7);
                }
            }

            throw var8;
        }

        if (lobCreator != null)
        {
            lobCreator.close();
        }

    }

    private void insertAuthorization(OAuth2Authorization authorization)
    {
        List<SqlParameterValue> parameters = (List)this.authorizationParametersMapper.apply(authorization);
        LobCreator lobCreator = this.lobHandler.getLobCreator();

        try
        {
            PreparedStatementSetter pss = new LobCreatorArgumentPreparedStatementSetter(lobCreator, parameters.toArray());
            this.jdbcOperations.update("INSERT INTO oauth2_authorization (id, registered_client_id, principal_name, authorization_grant_type, attributes, state, authorization_code_value, authorization_code_issued_at, authorization_code_expires_at,authorization_code_metadata,access_token_value,access_token_issued_at,access_token_expires_at,access_token_metadata,access_token_type,access_token_scopes,oidc_id_token_value,oidc_id_token_issued_at,oidc_id_token_expires_at,oidc_id_token_metadata,refresh_token_value,refresh_token_issued_at,refresh_token_expires_at,refresh_token_metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", pss);
        }
        catch (Throwable var7)
        {
            if (lobCreator != null)
            {
                try
                {
                    lobCreator.close();
                }
                catch (Throwable var6)
                {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (lobCreator != null)
        {
            lobCreator.close();
        }
    }

    private static final class LobCreatorArgumentPreparedStatementSetter extends ArgumentPreparedStatementSetter
    {
        private final LobCreator lobCreator;

        private LobCreatorArgumentPreparedStatementSetter(LobCreator lobCreator, Object[] args)
        {
            super(args);
            this.lobCreator = lobCreator;
        }

        protected void doSetValue(PreparedStatement ps, int parameterPosition, Object argValue) throws SQLException
        {
            if (argValue instanceof SqlParameterValue)
            {
                SqlParameterValue paramValue = (SqlParameterValue) argValue;

                if( paramValue.getSqlType() == -4 )
                {
                    // System.out.println( paramValue.getValue().toString() );

                    if( paramValue.getValue() != null )
                    {
                        // paramValue = new SqlParameterValue(12, paramValue.getValue().toString());
                        paramValue = new SqlParameterValue(2004, paramValue.getValue().toString().getBytes());
                        argValue = paramValue;
                    }
                }

                if (paramValue.getSqlType() == 2004)
                {
                    if (paramValue.getValue() != null)
                    {
                        Assert.isInstanceOf(byte[].class, paramValue.getValue(), "Value of blob parameter must be byte[]");
                    }

                    byte[] valueBytes = (byte[])paramValue.getValue();
                    this.lobCreator.setBlobAsBytes(ps, parameterPosition, valueBytes);
                    return;
                }

                if (paramValue.getSqlType() == 2005)
                {
                    if (paramValue.getValue() != null)
                    {
                        Assert.isInstanceOf(String.class, paramValue.getValue(), "Value of clob parameter must be String");
                    }

                    String valueString = (String)paramValue.getValue();
                    this.lobCreator.setClobAsString(ps, parameterPosition, valueString);
                    return;
                }
            }

            super.doSetValue(ps, parameterPosition, argValue);
        }
    }

    private void initColumnMetadata(JdbcOperations jdbcOperations)
    {
        columnMetadataMap = new HashMap();
        ColumnMetadata columnMetadata = getColumnMetadata(jdbcOperations, "attributes", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "authorization_code_value", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "authorization_code_metadata", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "access_token_value", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "access_token_metadata", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "oidc_id_token_value", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "oidc_id_token_metadata", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "refresh_token_value", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
        columnMetadata = getColumnMetadata(jdbcOperations, "refresh_token_metadata", 2004);
        columnMetadataMap.put(columnMetadata.getColumnName(), columnMetadata);
    }

    private final class ColumnMetadata
    {
        private final String columnName;
        private final int dataType;

        private ColumnMetadata(String columnName, int dataType)
        {
            this.columnName = columnName;
            this.dataType = dataType;
        }

        private String getColumnName() {
            return this.columnName;
        }

        private int getDataType() {
            return this.dataType;
        }
    }

    private ColumnMetadata getColumnMetadata(JdbcOperations jdbcOperations, String columnName, int defaultDataType)
    {
        Integer dataType = (Integer) this.jdbcOperations.execute( (ConnectionCallback<Integer>) (conn) ->
        {
            DatabaseMetaData databaseMetaData = conn.getMetaData();
            ResultSet rs = databaseMetaData.getColumns((String)null, (String)null, "oauth2_authorization", columnName);

            if (rs.next())
            {
                return rs.getInt("DATA_TYPE");
            }
            else
            {
                rs = databaseMetaData.getColumns((String)null, (String)null, "oauth2_authorization".toUpperCase(), columnName.toUpperCase());
                return rs.next() ? rs.getInt("DATA_TYPE") : null;
            }
        });
        return new ColumnMetadata(columnName, dataType != null ? dataType : defaultDataType);
    }

    private static SqlParameterValue mapToSqlParameter(String columnName, String value)
    {
        ColumnMetadata columnMetadata = (ColumnMetadata) columnMetadataMap.get(columnName);
        return 2004 == columnMetadata.getDataType() && StringUtils.hasText(value) ? new SqlParameterValue(2004, value.getBytes(StandardCharsets.UTF_8)) : new SqlParameterValue(columnMetadata.getDataType(), value);
    }

    private OAuth2Authorization findBy(String filter, List<SqlParameterValue> parameters)
    {
        LobCreator lobCreator = this.lobHandler.getLobCreator();

        OAuth2Authorization var6;

        System.out.println(this.authorizationRowMapper  );

        try
        {
            PreparedStatementSetter pss = new LobCreatorArgumentPreparedStatementSetter(lobCreator, parameters.toArray());
            List<OAuth2Authorization> result = this.jdbcOperations.query("SELECT id, registered_client_id, principal_name, authorization_grant_type, attributes, state, authorization_code_value, authorization_code_issued_at, authorization_code_expires_at,authorization_code_metadata,access_token_value,access_token_issued_at,access_token_expires_at,access_token_metadata,access_token_type,access_token_scopes,oidc_id_token_value,oidc_id_token_issued_at,oidc_id_token_expires_at,oidc_id_token_metadata,refresh_token_value,refresh_token_issued_at,refresh_token_expires_at,refresh_token_metadata FROM oauth2_authorization WHERE " + filter, pss, this.authorizationRowMapper);
            var6 = !result.isEmpty() ? (OAuth2Authorization)result.get(0) : null;
        }
        catch (Throwable var8)
        {
            if (lobCreator != null)
            {
                try
                {
                    lobCreator.close();
                }
                catch (Throwable var7)
                {
                    var8.addSuppressed(var7);
                }
            }

            throw var8;
        }

        if (lobCreator != null)
        {
            lobCreator.close();
        }

        return var6;
    }

    public static class OAuth2AuthorizationRowMapper implements RowMapper<OAuth2Authorization> {
        private final RegisteredClientRepository registeredClientRepository;
        private LobHandler lobHandler = new DefaultLobHandler();
        private ObjectMapper objectMapper = new ObjectMapper();

        public OAuth2AuthorizationRowMapper(RegisteredClientRepository registeredClientRepository) {
            Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
            this.registeredClientRepository = registeredClientRepository;
            ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
            List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
            this.objectMapper.registerModules(securityModules);
            this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        }

        public OAuth2Authorization mapRow(ResultSet rs, int rowNum) throws SQLException {
            String registeredClientId = rs.getString("registered_client_id");
            RegisteredClient registeredClient = this.registeredClientRepository.findById(registeredClientId);
            if (registeredClient == null) {
                throw new DataRetrievalFailureException("The RegisteredClient with id '" + registeredClientId + "' was not found in the RegisteredClientRepository.");
            } else {
                OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
                String id = rs.getString("id");
                String principalName = rs.getString("principal_name");
                String authorizationGrantType = rs.getString("authorization_grant_type");
                Map<String, Object> attributes = this.parseMap(this.getLobValue(rs, "attributes"));

                builder.id(id).principalName(principalName).authorizationGrantType(new AuthorizationGrantType(authorizationGrantType)).attributes((attrs) -> {
                    attrs.putAll(attributes);
                });
                builder.id(id).principalName(principalName).authorizationGrantType(new AuthorizationGrantType(authorizationGrantType));
                String state = rs.getString("state");
                if (StringUtils.hasText(state)) {
                    builder.attribute("state", state);
                }

                String authorizationCodeValue = this.getLobValue(rs, "authorization_code_value");
                Instant tokenIssuedAt;
                Instant tokenExpiresAt;
                if (StringUtils.hasText(authorizationCodeValue)) {
                    tokenIssuedAt = rs.getTimestamp("authorization_code_issued_at").toInstant();
                    tokenExpiresAt = rs.getTimestamp("authorization_code_expires_at").toInstant();
                    Map<String, Object> authorizationCodeMetadata = this.parseMap(this.getLobValue(rs, "authorization_code_metadata"));
                    OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(authorizationCodeValue, tokenIssuedAt, tokenExpiresAt);
                    builder.token(authorizationCode, (metadata) -> {
                        metadata.putAll(authorizationCodeMetadata);
                    });
                }

                String accessTokenValue = this.getLobValue(rs, "access_token_value");
                if (StringUtils.hasText(accessTokenValue)) {
                    tokenIssuedAt = rs.getTimestamp("access_token_issued_at").toInstant();
                    tokenExpiresAt = rs.getTimestamp("access_token_expires_at").toInstant();
                    Map<String, Object> accessTokenMetadata = this.parseMap(this.getLobValue(rs, "access_token_metadata"));
                    OAuth2AccessToken.TokenType tokenType = null;
                    if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(rs.getString("access_token_type"))) {
                        tokenType = OAuth2AccessToken.TokenType.BEARER;
                    }

                    Set<String> scopes = Collections.emptySet();
                    String accessTokenScopes = rs.getString("access_token_scopes");
                    if (accessTokenScopes != null) {
                        scopes = StringUtils.commaDelimitedListToSet(accessTokenScopes);
                    }

                    OAuth2AccessToken accessToken = new OAuth2AccessToken(tokenType, accessTokenValue, tokenIssuedAt, tokenExpiresAt, scopes);
                    builder.token(accessToken, (metadata) -> {
                        metadata.putAll(accessTokenMetadata);
                    });
                }

                String oidcIdTokenValue = this.getLobValue(rs, "oidc_id_token_value");
                if (StringUtils.hasText(oidcIdTokenValue)) {
                    tokenIssuedAt = rs.getTimestamp("oidc_id_token_issued_at").toInstant();
                    tokenExpiresAt = rs.getTimestamp("oidc_id_token_expires_at").toInstant();
                    Map<String, Object> oidcTokenMetadata = this.parseMap(this.getLobValue(rs, "oidc_id_token_metadata"));
                    OidcIdToken oidcToken = new OidcIdToken(oidcIdTokenValue, tokenIssuedAt, tokenExpiresAt, (Map)oidcTokenMetadata.get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME));
                    builder.token(oidcToken, (metadata) -> {
                        metadata.putAll(oidcTokenMetadata);
                    });
                }

                String refreshTokenValue = this.getLobValue(rs, "refresh_token_value");
                if (StringUtils.hasText(refreshTokenValue)) {
                    tokenIssuedAt = rs.getTimestamp("refresh_token_issued_at").toInstant();
                    tokenExpiresAt = null;
                    Timestamp refreshTokenExpiresAt = rs.getTimestamp("refresh_token_expires_at");
                    if (refreshTokenExpiresAt != null) {
                        tokenExpiresAt = refreshTokenExpiresAt.toInstant();
                    }

                    Map<String, Object> refreshTokenMetadata = this.parseMap(this.getLobValue(rs, "refresh_token_metadata"));
                    OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(refreshTokenValue, tokenIssuedAt, tokenExpiresAt);
                    builder.token(refreshToken, (metadata) -> {
                        metadata.putAll(refreshTokenMetadata);
                    });
                }

                return builder.build();
            }
        }

        private String getLobValue(ResultSet rs, String columnName) throws SQLException {
            String columnValue = null;
            ColumnMetadata columnMetadata = (ColumnMetadata) columnMetadataMap.get(columnName);

            if ( 2004 == columnMetadata.getDataType() || "attributes".equals(columnName) )
            {
                byte[] columnValueBytes = this.lobHandler.getBlobAsBytes(rs, columnName);

                if (columnValueBytes != null)
                {
                    columnValue = new String(columnValueBytes, StandardCharsets.UTF_8);
                }
            } else if (2005 == columnMetadata.getDataType()) {
                columnValue = this.lobHandler.getClobAsString(rs, columnName);
            } else {
                columnValue = rs.getString(columnName);
            }

            return columnValue;
        }

        public final void setLobHandler(LobHandler lobHandler) {
            Assert.notNull(lobHandler, "lobHandler cannot be null");
            this.lobHandler = lobHandler;
        }

        public final void setObjectMapper(ObjectMapper objectMapper) {
            Assert.notNull(objectMapper, "objectMapper cannot be null");
            this.objectMapper = objectMapper;
        }

        protected final RegisteredClientRepository getRegisteredClientRepository() {
            return this.registeredClientRepository;
        }

        protected final LobHandler getLobHandler() {
            return this.lobHandler;
        }

        protected final ObjectMapper getObjectMapper() {
            return this.objectMapper;
        }

        private Map<String, Object> parseMap(String data)
        {
            try
            {
                return (Map)this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
            }
            catch (Exception var3)
            {
                throw new IllegalArgumentException(var3.getMessage(), var3);
            }
        }
    }
}
