package co.freeside.betamax.proxy.netty

import com.google.common.base.Predicate
import io.netty.handler.codec.http.*
import org.littleshoot.proxy.HttpFilters
import spock.lang.*
import static io.netty.handler.codec.http.HttpMethod.GET
import static io.netty.handler.codec.http.HttpResponseStatus.OK
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1

@Unroll
class PredicatedHttpFiltersSpec extends Specification {

	@Subject
	PredicatedHttpFilters filters

	Predicate<HttpRequest> predicate = Stub(Predicate)
	def delegate = Mock(HttpFilters)
	def request = new DefaultHttpRequest(HTTP_1_1, GET, "http://freeside.co/betamax")
	def response = new DefaultHttpResponse(HTTP_1_1, OK)
	def httpObj = new DefaultLastHttpContent()

	void setup() {
		filters = new PredicatedHttpFilters(delegate, predicate, request)
	}

	void "request#method does not call delegate.request#method when the predicate returns false"() {
		given:
		predicate.apply(request) >> false

		when:
		def result = filters."request$method" httpObj

		then:
		result == null

		and:
		0 * delegate._

		where:
		method << ["Pre", "Post"]
	}

	void "response#method does not call delegate.response#method when the predicate returns false"() {
		given:
		predicate.apply(request) >> false

		when:
		filters."response$method" httpObj

		then:
		0 * delegate._

		where:
		method << ["Pre", "Post"]
	}

	void "request#method calls delegate.request#method when the predicate returns true"() {
		given:
		predicate.apply(request) >> true

		when:
		def result = filters."request$method" httpObj

		then:
		result == response

		and:
		1 * delegate."request$method"(httpObj) >> response

		where:
		method << ["Pre", "Post"]
	}

	void "response#method calls delegate.response#method when the predicate returns true"() {
		given:
		predicate.apply(request) >> true

		when:
		filters."response$method" httpObj

		then:
		1 * delegate."response$method"(httpObj)

		where:
		method << ["Pre", "Post"]
	}

}
