package com.myapp.repository

import com.myapp.config.DatasourceConfig
import com.myapp.domain.Micropost
import com.myapp.domain.Relationship
import com.myapp.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

@Transactional
@ActiveProfiles("test")
@ContextConfiguration(classes = [RepositoryTestConfig, DatasourceConfig])
class MicropostRepositoryTest extends Specification {

    @Autowired
    MicropostRepository micropostRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    RelationshipRepository relationshipRepository

    def "can find feed"() {
        given:
        User follower = userRepository.save(new User(username: "akira@test.com", password: "secret", name: "akira"))
        User followed = userRepository.save(new User(username: "test1@test.com", password: "secret", name: "test1"))
        User another = userRepository.save(new User(username: "test2@test.com", password: "secret", name: "test2"))
        relationshipRepository.save(new Relationship(follower: follower, followed: followed))
        [follower, followed, another].each { u ->
            micropostRepository.save(new Micropost(content: "test1", user: u))
            micropostRepository.save(new Micropost(content: "test2", user: u))
        }

        when:
        List<Micropost> result = micropostRepository
                .findAsFeed(follower, Optional.empty(), Optional.empty(), null)

        then:
        result.size() == 4
        result.first().user == followed
        result.last().user == follower
    }

    def "can find feed by since_id or max_id"() {
        given:
        User user = userRepository.save(new User(username: "akira@test.com", password: "secret", name: "akira"))
        Micropost post1 = micropostRepository.save(new Micropost(content: "test1", user: user))
        Micropost post2 = micropostRepository.save(new Micropost(content: "test2", user: user))
        Micropost post3 = micropostRepository.save(new Micropost(content: "test3", user: user))

        when:
        List<Micropost> result = micropostRepository
                .findAsFeed(user, Optional.of(post2.id), Optional.empty(), null)

        then:
        result.size() == 1
        result.first() == post3

        when:
        result = micropostRepository
                .findAsFeed(user, Optional.empty(), Optional.of(post2.id), null)

        then:
        result.size() == 1
        result.first() == post1
    }

    def "can find posts by user"() {
        given:
        User user = userRepository.save(new User(username: "akira@test.com", password: "secret", name: "akira"))
        Micropost post1 = micropostRepository.save(new Micropost(content: "test1", user: user))
        Micropost post2 = micropostRepository.save(new Micropost(content: "test2", user: user))
        Micropost post3 = micropostRepository.save(new Micropost(content: "test3", user: user))

        when:
        List<Micropost> result = micropostRepository
                .findByUser(user, Optional.of(post2.id), Optional.empty(), null)

        then:
        result.size() == 1
        result.first() == post3

        when:
        result = micropostRepository
                .findByUser(user, Optional.empty(), Optional.of(post2.id), null)

        then:
        result.size() == 1
        result.first() == post1
    }

}