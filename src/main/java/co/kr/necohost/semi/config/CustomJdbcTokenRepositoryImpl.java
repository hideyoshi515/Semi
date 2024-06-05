package co.kr.necohost.semi.config;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.util.Date;

public class CustomJdbcTokenRepositoryImpl extends JdbcTokenRepositoryImpl {
    private static final String TOKEN_BY_SERIES_SQL = "select email, series, token, last_used from persistent_logins where series = ?";
    private static final String INSERT_TOKEN_SQL = "insert into persistent_logins (email, series, token, last_used) values(?,?,?,?)";
    private static final String UPDATE_TOKEN_SQL = "update persistent_logins set token = ?, last_used = ? where series = ?";
    private static final String REMOVE_USER_TOKENS_SQL = "delete from persistent_logins where email = ?";

    private final JdbcTemplate jdbcTemplate;

    public CustomJdbcTokenRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        setDataSource(dataSource);
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        System.out.println("[I]username:"+token.getUsername());
        System.out.println("[I]series:"+token.getSeries());
        System.out.println("[I]tokenValue:"+token.getTokenValue());
        jdbcTemplate.update(INSERT_TOKEN_SQL, token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        System.out.println("[U]series:"+series);
        System.out.println("[U]tokenValue:"+tokenValue);
        jdbcTemplate.update(UPDATE_TOKEN_SQL, tokenValue, lastUsed, series);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        try {
            PersistentRememberMeToken result = jdbcTemplate.queryForObject(TOKEN_BY_SERIES_SQL, (rs, rowNum) -> new PersistentRememberMeToken(
                    rs.getString("email"),
                    rs.getString("series"),
                    rs.getString("token"),
                    rs.getTimestamp("last_used")
            ), seriesId);
            System.out.println("[S]username:"+result.getUsername());
            System.out.println("[S]series:"+result.getSeries());
            System.out.println("[S]tokenValue:"+result.getTokenValue());
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void removeUserTokens(String username) {
        jdbcTemplate.update(REMOVE_USER_TOKENS_SQL, username);
    }
}
