class Main {

	static main(args) {
		
		def props = new CommandLineParser().parse(args)
		
		def feedly = new Feedly(props.devtoken, props.userid, props.verbose)
		
		if (props.labels)
			Printer.printAsJson("Labels", feedly.getLabels())
			
		if (props.tags)
			Printer.printAsJson("Tags", feedly.getTags())

		if (!props.posts.isEmpty()) {
			def posts = feedly.getPosts(props.posts, props.maxposts)
			if (props.verbose) { 
				Printer.printAsJson("[INFO] Posts with label [$props.posts]", posts)
			} else {
				println "Posts with label [$props.posts]"
				posts.each { post -> post.enclosure?.each { println it } }
			}

			if (props.media) {
				posts.each { post -> post.enclosure?.each { 
                    if (it.href.contains("youtube")) {
                        if (props.youtube)
					        if (!Downloader.downloadYoutube(it.href))
                                return
                        if (props.youtubeaudio)
					        if (!Downloader.downloadYoutubeAudio(it.href))
                                return
                    }
                    if (props.downloader == 'wget') {
    					if (Downloader.downloadMediaWithWGet(it.href, it.length))
    						feedly.unsavePost(post.id)
                    } else {
    					if (Downloader.downloadMediaWithEmbedded(it.href, it.length, props.verbose))
    						feedly.unsavePost(post.id)
                    }
				}}
			}
			if (props.youtube) {
				posts.each { post -> post.embeddedVideo?.each {
					if (Downloader.downloadYoutube(it))
						if (!props.youtubeaudio)
							feedly.unsavePost(post.id)
				}}
			}
			if (props.youtubeaudio) {
				posts.each { post -> post.embeddedVideo?.each {
					if (Downloader.downloadYoutubeAudio(it))
						feedly.unsavePost(post.id)
				}}
			}
		}
			
		if (props.posts.isEmpty() && (props.media || props.youtube))
			println "[WARN] You can only download media and youtube videos from posts. Please specify -posts first."

	}

}
