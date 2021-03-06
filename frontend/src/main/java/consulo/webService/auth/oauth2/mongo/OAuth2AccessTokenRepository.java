package consulo.webService.auth.oauth2.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import consulo.webService.auth.oauth2.domain.OAuth2AuthenticationAccessToken;

/**
 * @version 1.0
 * @author: Iain Porter
 * @since 22/05/2013
 */
@Repository
public interface OAuth2AccessTokenRepository extends MongoRepository<OAuth2AuthenticationAccessToken, String>
{
	OAuth2AuthenticationAccessToken findByTokenId(String tokenId);

	OAuth2AuthenticationAccessToken findByAuthenticationIdAndName(String authenticationId, String name);

	OAuth2AuthenticationAccessToken findByUserNameAndName(String userName, String name);

	List<OAuth2AuthenticationAccessToken> findByClientIdAndUserName(String clientId, String userName);

	List<OAuth2AuthenticationAccessToken> findByClientId(String clientId);
}