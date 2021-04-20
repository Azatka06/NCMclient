package ru.sagutdinov

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_post.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.sagutdinov.authorization.Repository
import java.text.SimpleDateFormat

class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(adapter: PostAdapter, position: Int, post: Post) {
        with(itemView) {
            val format = SimpleDateFormat("dd/MM/yyyy")
            when (post.postType) {
                PostType.Post -> {
                    locationButton.isVisible = false
                    postImage.isVisible = false
                }
                PostType.Event -> {
                    locationButton.setOnClickListener {
                        val geo = Intent().apply {
                            data = Uri.parse("geo:${post.location?.first}${post.location?.second}")
                        }
                        context.startActivity(geo)
                    }
                    locationButton.isVisible = true
                    postImage.isVisible = false

                }
                PostType.Video -> {
                    postImage.setImageDrawable(post.postImage.let {
                        getDrawableFromAbstractInt(
                            context,
                            0
                        )
                    })
                    postImage.setOnClickListener {
                        val watchVideo = Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(post.link)
                        }
                        context.startActivity(watchVideo)
                    }
                    locationButton.isVisible = false
                    postImage.isVisible = true

                }
                PostType.Advertising -> {
                    postImage.setImageDrawable(post.postImage?.let {
                        getDrawableFromAbstractInt(
                            context,
                            it
                        )
                    })
                    postImage.setOnClickListener {
                        val watchVideo = Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(post.link)
                        }
                        context.startActivity(watchVideo)
                    }
                    locationButton.isVisible = false
                    postImage.isVisible = true
                }
            }
            commentNumber.text = post.commentCounter.toString()
            shareNumber.text = post.shareCounter.toString()
            authorIcon.setImageDrawable(getDrawableFromAbstractInt(context, post.authorDrawable))
            authorName.text = post.authorName
            postBody.text = post.bodyText
            postDate.text = format.format(post.postDate)
            likeNumber.text = if (post.likeCounter <= 0) {
                ""
            } else {
                post.likeCounter.toString()
            }
            if (post.likedByMe) {
                likeButton.setImageDrawable(getDrawable(context, R.drawable.ic_favorite_red_24dp))
            } else {
                likeButton.setImageDrawable(getDrawable(context, R.drawable.ic_favorite_grey_24dp))
            }
            dislikeNumber.text = if (post.dislikeCounter <= 0) {
                ""
            } else {
                post.dislikeCounter.toString()
            }
            if (post.dislikedByMe) {
                dislikeButton.setImageDrawable(getDrawable(context, R.drawable.ic_dislike_red))
            } else {
                dislikeButton.setImageDrawable(getDrawable(context, R.drawable.ic_dislike_grey))
            }
            likeButton.setOnClickListener {
                post.likedByMe = if (!post.likedByMe) {
                    likeButton.setImageDrawable(
                        getDrawable(
                            context,
                            R.drawable.ic_favorite_red_24dp
                        )
                    )
                    post.likeIncrease()
                    CoroutineScope(IO).launch {
                        changeCounter(post, CounterType.Like, adapter.token)
                        this.cancel()
                    }
                    adapter.notifyItemChanged(position)
                    true
                } else {
                    likeButton.setImageDrawable(
                        getDrawable(
                            context,
                            R.drawable.ic_favorite_grey_24dp
                        )
                    )
                    post.likeDecrease()
                    CoroutineScope(IO).launch {
                        changeCounter(post, CounterType.Like, adapter.token)
                        this.cancel()
                    }
                    adapter.notifyItemChanged(position)
                    false
                }
            }
            dislikeButton.setOnClickListener {
                post.dislikedByMe = if (!post.dislikedByMe) {
                    dislikeButton.setImageDrawable(
                        getDrawable(
                            context,
                            R.drawable.ic_dislike_red
                        )
                    )
                    post.dislikeIncrease()
                    CoroutineScope(IO).launch {
                        changeCounter(post, CounterType.Dislike, adapter.token)
                        this.cancel()
                    }
                    adapter.notifyItemChanged(position)
                    true
                } else {
                    dislikeButton.setImageDrawable(
                        getDrawable(
                            context,
                            R.drawable.ic_dislike_grey
                        )
                    )
                    post.dislikeDecrease()
                    CoroutineScope(IO).launch {
                        changeCounter(post, CounterType.Dislike, adapter.token)
                        this.cancel()
                    }
                    adapter.notifyItemChanged(position)
                    false
                }
            }
            shareButton.setOnClickListener {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT, """${post.authorName} (${post.postDate})
                                    ${post.bodyText}""".trimIndent()
                    )
                    type = "text/plain"
                }
                post.shareIncrease()
                context.startActivity(intent)
                CoroutineScope(IO).launch {
                    changeCounter(post, CounterType.Share, adapter.token)
                    this.cancel()
                }
            }
        }
    }

    private fun getDrawableFromAbstractInt(context: Context, value: Int) =
        when (value) {
            0 -> getDrawable(context, R.mipmap.ic_launcher)
            1 -> getDrawable(context, R.mipmap.ic_launcher_round)
            2 -> getDrawable(context, R.drawable.ic_health)
            else -> getDrawable(context, R.drawable.ic_health)
        }

    private suspend fun changeCounter(post: Post, type: CounterType, token:String) {
        when (type) {
            CounterType.Like -> {
                Repository.changeCounter(token ,CounterChangeDto(post.id, post.likeCounter, type))
            }
            CounterType.Dislike -> {
                Repository.changeCounter(token ,CounterChangeDto(post.id, post.dislikeCounter, type))
            }
            CounterType.Comment -> {
                Repository.changeCounter(token,CounterChangeDto(post.id, post.commentCounter, type))
            }
            CounterType.Share -> {
                Repository.changeCounter(token,CounterChangeDto(post.id, post.shareCounter, type))
            }
        }
    }
}
